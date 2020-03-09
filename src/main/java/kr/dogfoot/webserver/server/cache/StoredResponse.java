package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.*;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.CacheDirective;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.CacheDirectiveSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.WarningValue;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.response.StatusCode;
import kr.dogfoot.webserver.httpMessage.util.ResponseSetter;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.host.proxy_info.CacheOption;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRange;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StoredResponse {
    private static ConcurrentLinkedQueue<StoredResponse> srPool = new ConcurrentLinkedQueue<StoredResponse>();

    public static StoredResponse pooledObject(CacheEntry entry) {
        StoredResponse sr = srPool.poll();
        if (sr == null) {
            sr = new StoredResponse();
        }
        sr.reset();
        sr.entry = entry;
        return sr;
    }

    public static void release(StoredResponse sr) {
        srPool.add(sr);
    }

    private CacheEntry entry;
    private MethodType requestMethod;
    private HeaderValueVary varyHeader;
    private Set<HeaderValue> selectionFields;
    private byte[] etag;
    private long date;
    private long age;
    private long requestTime;
    private long responseTime;
    private long freshnessLifeTime;
    private Response response;
    private File infoFile;
    private File responseFile;
    private File bodyFile;

    private Object lockForProperty = new Object();
    private Object lockForResponse = new Object();
    private Object lockForTimerOrUsingCount = new Object();
    private Object lockForFile = new Object();

    private Object timerEventForInactiveTimeout;
    private int usingCount;

    private StoredResponse() {
        selectionFields = new HashSet<HeaderValue>();
    }

    private void reset() {
        entry = null;

        requestMethod = null;
        varyHeader = null;
        selectionFields.clear();

        etag = null;
        date = 0;
        age = 0;

        requestTime = 0;
        responseTime = 0;
        freshnessLifeTime = 0;

        response = null;

        infoFile = null;
        responseFile = null;
        bodyFile = null;

        timerEventForInactiveTimeout = null;
        usingCount = 0;
    }

    public void set(Request request, Response response, CacheOption cacheOption) {
        set_Core(request, response, cacheOption);
        response(response);
    }

    private void set_Core(Request request, Response response, CacheOption cacheOption) {
        synchronized (lockForProperty) {
            requestMethod = request.method();
            setVaryHeader(response);
            setSelectionFields(request);

            etag = getETagHeaderValue(response);
            date = getDateHeaderValue(response);
            age = getAgeHeaderValue(response);

            requestTime = request.requestTime();
            responseTime = response.responseTime();
            freshnessLifeTime = calculateFreshnessLifeTime(response, cacheOption);
        }
    }

    private void setSelectionFields(Request request) {
        selectionFields.clear();
        if (varyHeader != null && varyHeader.isAsterisk() == false) {
            for (HeaderSort headerSort : varyHeader.fieldNames()) {
                HeaderValue value = request.getHeaderValueObj(headerSort);
                if (value != null) {
                    selectionFields.add(value);
                }
            }
        }
    }

    private void setVaryHeader(Response response) {
        if (response != null) {
            varyHeader = (HeaderValueVary) response.getHeaderValueObj(HeaderSort.Vary);
        }
    }

    private byte[] getETagHeaderValue(Response response) {
        HeaderValueETag eTag = (HeaderValueETag) response.getHeaderValueObj(HeaderSort.ETag);
        if (eTag != null) {
            return eTag.etag();
        } else {
            return null;
        }
    }

    private long getDateHeaderValue(Response response) {
        HeaderValueDate date = (HeaderValueDate) response.getHeaderValueObj(HeaderSort.Date);
        if (date != null) {
            return date.date();
        }
        return -1;
    }

    private long getAgeHeaderValue(Response response) {
        HeaderValueAge age = (HeaderValueAge) response.getHeaderValueObj(HeaderSort.Age);
        if (age != null) {
            return age.ageValue();
        }
        return 0;
    }

    private long calculateFreshnessLifeTime(Response response, CacheOption cacheOption) {
        HeaderValueCacheControl cacheControl = (HeaderValueCacheControl) response.getHeaderValueObj(HeaderSort.Cache_Control);
        if (cacheControl != null) {
            CacheDirective s_maxage = cacheControl.getCacheDirective(CacheDirectiveSort.SMaxAge);
            if (s_maxage != null) {
                return s_maxage.deltaSeconds();
            }
            CacheDirective max_age = cacheControl.getCacheDirective(CacheDirectiveSort.MaxAge);
            if (max_age != null) {
                return max_age.deltaSeconds();
            }
        }
        HeaderValueDate date = (HeaderValueDate) response.getHeaderValueObj(HeaderSort.Date);
        if (date != null) {
            HeaderValueExpires expires = (HeaderValueExpires) response.getHeaderValueObj(HeaderSort.Expires);
            if (expires != null) {
                return (expires.date() - date.date()) / 1000;
            }

            HeaderValueLastModified lastModified = (HeaderValueLastModified) response.getHeaderValueObj(HeaderSort.Last_Modified);
            if (lastModified != null) {
                return ((date.date() - lastModified.date()) / 10) / 1000;
            }
        }
        return cacheOption.defaultExpires();
    }

    private void response(Response response) {
        synchronized (lockForResponse) {
            this.response = response;
        }
    }

    public void update(Request request, Response response, CacheOption cacheOption) {
        updateWarningFromStoredResponse();
        moveResponseHeader(response);
        set_Core(request, response, cacheOption);
    }

    private void updateWarningFromStoredResponse() {
        synchronized (lockForResponse) {
            ArrayList<HeaderItem> removeHeaderItems = new ArrayList<HeaderItem>();

            int count = response.headerCount();
            for (int index = 0; index < count; index++) {
                HeaderItem item = response.getHeaderItem(index);
                if (item.sort() == HeaderSort.Warning) {
                    HeaderValueWarning warning = (HeaderValueWarning) item.valueObj();
                    removeWarningCode1xx(warning);

                    if (warning.warningValueList().size() == 0) {
                        removeHeaderItems.add(item);
                    }
                }
            }

            for (HeaderItem item : removeHeaderItems) {
                response.removeHeader(item);
            }
        }
    }

    private void removeWarningCode1xx(HeaderValueWarning warning) {
        ArrayList<WarningValue> removeWarningValues = new ArrayList<WarningValue>();
        for (WarningValue warningValue : warning.warningValueList()) {
            if (warningValue.code().is1xx()) {
                removeWarningValues.add(warningValue);
            }
        }
        warning.warningValueList().removeAll(removeWarningValues);
    }

    private void moveResponseHeader(Response response) {
        synchronized (lockForResponse) {
            int count = response.headerCount();
            for (int index = 0; index < count; index++) {
                HeaderItem item = response.getHeaderItem(index);
                ResponseSetter.setHeader(this.response, item.sort(), item.valueBytes());
            }
        }
    }

    public void deleteMe() {
        entry.manager().sizeLimiter().subtractSize(getStorageSize());
    }

    private long getStorageSize() {
        synchronized (lockForFile) {
            synchronized (lockForResponse) {
                return infoFile.length() + responseFile.length() + response.contentLength();
            }
        }
    }

    public boolean isMatchMethodAndSelectedFields(Request request) {
        synchronized (lockForProperty) {
            if (request.method() == requestMethod
                    && isMatchSelectedFields(request)) {
                return true;
            }
            return false;
        }
    }

    private boolean isMatchSelectedFields(Request request) {
        synchronized (lockForProperty) {
            if (varyHeader == null) {
                return true;
            }
            if (varyHeader.isAsterisk() == true) {
                return false;
            }

            int equalCount = 0;
            for (HeaderValue value : selectionFields) {
                if (request.hasHeader(value.sort())) {
                    HeaderValue value2 = request.getHeaderValueObj(value.sort());
                    if (value.isEqualValue(value2)) {
                        equalCount++;
                    }
                }
            }
            if (equalCount == selectionFields.size()) {
                return true;
            }
            return false;
        }
    }

    public boolean isFresh(Request request) {
        synchronized (lockForProperty) {
            HeaderValueCacheControl cacheControl = (HeaderValueCacheControl) request.getHeaderValueObj(HeaderSort.Cache_Control);
            if (cacheControl != null) {
                CacheDirective maxAge = cacheControl.getCacheDirective(CacheDirectiveSort.MaxAge);
                if (maxAge != null) {
                    return currentAge() <= maxAge.deltaSeconds();
                }
                CacheDirective maxState = cacheControl.getCacheDirective(CacheDirectiveSort.MaxStale);
                if (maxState != null) {
                    return freshnessLifeTime <= maxState.deltaSeconds();
                }
                CacheDirective minFresh = cacheControl.getCacheDirective(CacheDirectiveSort.MaxStale);
                if (minFresh != null) {
                    return freshnessLifeTime >= currentAge() + minFresh.deltaSeconds();
                }
            }
            return freshnessLifeTime >= currentAge();
        }
    }

    public synchronized long currentAge() {
        synchronized (lockForProperty) {
            long apparentAge = Math.max(0, responseTime - date);
            long responseDelay = responseTime - requestTime;
            long correctedAgeValue = age * 1000 + responseDelay;
            long correctedInitalAge = Math.max(apparentAge, correctedAgeValue);
            long residentTime = now() - responseTime;
            long currentAge = (correctedInitalAge + residentTime) / 1000;
            return currentAge;
        }
    }

    private long now() {
        return new Date().getTime();
    }

    public void setTimerForInactiveTimeout() {
        synchronized (lockForTimerOrUsingCount) {
            if (timerEventForInactiveTimeout != null) {
                entry.manager().timer().removeEvent(timerEventForInactiveTimeout);
            }
            timerEventForInactiveTimeout = entry.manager().timer().addEvent(entry.manager().inactiveTimeout(), entry, this);
        }
    }

    public void unsetTimerForInactiveTimeout() {
        synchronized (lockForTimerOrUsingCount) {
            if (timerEventForInactiveTimeout != null) {
                entry.manager().timer().removeEvent(timerEventForInactiveTimeout);
                timerEventForInactiveTimeout = null;
            }
        }
    }

    public void lockUsing() {
        synchronized (lockForTimerOrUsingCount) {
            usingCount++;
            setTimerForInactiveTimeout();
        }
    }

    public void freeUsing() {
        synchronized (lockForTimerOrUsingCount) {
            usingCount--;
            setTimerForInactiveTimeout();
        }
    }

    public int usingCount() {
        synchronized (lockForTimerOrUsingCount) {
            return usingCount;
        }
    }

    public byte[] etag() {
        synchronized (lockForProperty) {
            return etag;
        }
    }

    public long date() {
        synchronized (lockForProperty) {
            return date;
        }
    }

    public long responseTime() {
        synchronized (lockForProperty) {
            return responseTime;
        }
    }
    public Response response() {
        return response;
    }

    public void addTotalSize() {
        entry.manager().sizeLimiter().addSize(getStorageSize());
    }

    public File bodyFile() {
        synchronized (lockForFile) {
            return bodyFile;
        }
    }

    public void storeToFile() {
        synchronized (lockForFile) {
            if (infoFile == null) {
                createInfoFileWithTempFile();
                createResource_Body_File();
            }

            storeInfoFile();
            storeResponseFile();
        }
    }

    private void createInfoFileWithTempFile() {
        try {
            infoFile = File.createTempFile("csr", ".inf", entry.directoryFile());
        } catch (IOException e) {
            infoFile = null;
        }
    }

    private void createResource_Body_File() {
        if (infoFile != null) {
            String infoFileName = infoFile.getName();
            responseFile = new File(infoFile.getParentFile(), infoFileName.substring(0, infoFileName.length() - 4) + ".rsp");
            bodyFile = new File(infoFile.getParentFile(), infoFileName.substring(0, infoFileName.length() - 4) + ".bdy");
        }
    }

    private void storeInfoFile() {
        synchronized (lockForProperty) {
            if (infoFile == null) {
                return;
            }
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(requestMethod);
                    oos.writeObject(selectionFields.size());
                    for (HeaderValue field : selectionFields) {
                        oos.writeObject(field.sort());
                        oos.writeObject(field.combineValue());
                    }
                    oos.writeObject(etag);
                    oos.writeObject(date);
                    oos.writeObject(age);
                    oos.writeObject(requestTime);
                    oos.writeObject(responseTime);
                    oos.writeObject(freshnessLifeTime);

                    FileOutputStream fos = new FileOutputStream(infoFile);
                    fos.write(baos.toByteArray());
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void storeResponseFile() {
        synchronized (lockForResponse) {
            if (responseFile == null) {
                return;
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                    oos.writeObject(response.majorVersion());
                    oos.writeObject(response.minorVersion());
                    oos.writeObject(response.statusCode());
                    int headerCount = response.headerCount();
                    oos.writeObject(headerCount);
                    for (int index = 0; index < headerCount; index++) {
                        HeaderItem item = response.getHeaderItem(index);
                        oos.writeObject(item.sort());
                        oos.writeObject(item.valueBytes());
                    }

                    FileOutputStream fos = new FileOutputStream(responseFile);
                    fos.write(baos.toByteArray());
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void createCacheFileFromInfoFile(File infoFile) {
        synchronized (lockForFile) {
            this.infoFile = infoFile;
            createResource_Body_File();
        }
    }

    public void loadFromFile(File infoFile) {
        synchronized (lockForFile) {
            this.infoFile = infoFile;
            createResource_Body_File();

            loadResponseFile();
            if (response != null) {
                loadInfoFile();
            }
        }
    }

    public void loadInfoFile() {
        synchronized (lockForProperty) {
            if (infoFile == null) {
                return;
            }
            try (FileInputStream fis = new FileInputStream(infoFile)) {
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    requestMethod = (MethodType) ois.readObject();
                    int selectFieldCount = (int) ois.readObject();
                    for (int index = 0; index < selectFieldCount; index++) {
                        HeaderSort sort = (HeaderSort) ois.readObject();
                        byte[] value = (byte[]) ois.readObject();

                        HeaderValue headerValue = FactoryForHeaderValue.create(sort);
                        try {
                            headerValue.parseValue(value);
                            selectionFields.add(headerValue);
                        } catch (ParserException e) {
                        }
                    }

                    etag = (byte[]) ois.readObject();
                    date = (Long) ois.readObject();
                    age = (Long) ois.readObject();
                    requestTime = (Long) ois.readObject();
                    responseTime = (Long) ois.readObject();
                    freshnessLifeTime = (Long) ois.readObject();

                    ois.close();
                    fis.close();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadResponseFile() {
        synchronized (lockForResponse) {
            if (responseFile == null) {
                return;
            }

            try (FileInputStream fis = new FileInputStream(responseFile)) {
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    response = new Response();
                    response.majorVersion((Short) ois.readObject());
                    response.minorVersion((Short) ois.readObject());
                    response.statusCode((StatusCode) ois.readObject());

                    int headerCount = (int) ois.readObject();
                    for (int index = 0; index < headerCount; index++) {
                        HeaderSort sort = (HeaderSort) ois.readObject();
                        byte[] value = (byte[]) ois.readObject();

                        response.addHeader(sort, value);
                    }
                    if (response.range() == null) {
                        response.range(new ContentRange());
                    }
                    response.range().lastPos(bodyFile.length() - 1);
                    setVaryHeader(response);

                    ois.close();
                    fis.close();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                response = null;
            }
        }
    }

    public void storeBody(ByteBuffer buffer) {
        synchronized (lockForResponse) {
            if (bodyFile == null) {
                return;
            }

            FileChannel wChannel = null;
            try {
                wChannel = new FileOutputStream(bodyFile, true).getChannel();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                wChannel = null;
            }

            if (wChannel != null) {
                try {
                    wChannel.write(buffer);
                    wChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        updateResponseRange();
    }

    private void updateResponseRange() {
        synchronized (lockForResponse) {
            if (response.range() == null) {
                response.range(new ContentRange());
            }
            response.range().lastPos(bodyFile.length() - 1);
        }
    }

    public void deleteFile(boolean includeBodyFile) {
        synchronized (lockForFile) {
            if (infoFile != null) {
                infoFile.delete();
                infoFile = null;
            }
            if (responseFile != null) {
                responseFile.delete();
                responseFile = null;
            }
            if (includeBodyFile == true && bodyFile != null) {
                bodyFile.delete();
                bodyFile = null;
            }
        }
    }
}
