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
import kr.dogfoot.webserver.httpMessage.util.ResponseSetter;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StoredResponse {
    private static ConcurrentLinkedQueue<StoredResponse> srPool = new ConcurrentLinkedQueue<StoredResponse>();
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
    private Object timerEventForRemove;
    private int usingCount;

    private StoredResponse() {
        selectionFields = new HashSet<HeaderValue>();
    }

    public static StoredResponse pooledObject() {
        StoredResponse sr = srPool.poll();
        if (sr == null) {
            sr = new StoredResponse();
        }
        sr.reset();
        return sr;
    }

    public static void release(StoredResponse sr) {
        srPool.add(sr);
    }

    public void set(Request request, Response response, CacheEntry entry) {
        requestMethod = request.method();
        setVaryHeaderAndSelectionFields(request, response);

        etag = getETagHeaderValue(response);
        date = getDateHeaderValue(response);
        age = getAgeHeaderValue(response);

        requestTime = request.requestTime();
        responseTime = response.responseTime();
        freshnessLifeTime = calculateFreshnessLifeTime(response);

        this.response = response;
        if (entry != null) {
            this.entry = entry;
        }
    }

    private void setVaryHeaderAndSelectionFields(Request request, Response response) {
        setVaryHeader(response);

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

    public void setVaryHeader(Response response) {
        if (response != null) {
            varyHeader = (HeaderValueVary) response.getHeaderValueObj(HeaderSort.Vary);
        }
    }

    public void addSelectField(HeaderValue headerValue) {
        selectionFields.add(headerValue);
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

    private long calculateFreshnessLifeTime(Response response) {
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

            // heuristic calculate
            HeaderValueLastModified lastModified = (HeaderValueLastModified) response.getHeaderValueObj(HeaderSort.Last_Modified);
            if (lastModified != null) {
                return ((date.date() - lastModified.date()) / 10) / 1000;
            }
        }
        return -1;
    }

    public void update(Request request, Response response) {
        updateWarningFromStoredResponse();
        moveResponseHeader(response);
        setForUpdate(request, response);

        StoredResponseStorer.deleteTempFile(this, false);
        StoredResponseStorer.store(this);
    }

    private void updateWarningFromStoredResponse() {
        ArrayList<HeaderItem> removeHeaderItem = new ArrayList<HeaderItem>();

        int count = response.headerCount();
        for (int index = 0; index < count; index++) {
            HeaderItem item = response.getHeaderItem(index);
            if (item.sort() == HeaderSort.Warning) {
                HeaderValueWarning warning = (HeaderValueWarning) item.valueObj();
                removeWarningCode1xx(warning);

                if (warning.warningValueList().size() == 0) {
                    removeHeaderItem.add(item);
                }
            }
        }

        for (HeaderItem item : removeHeaderItem) {
            response.removeHeader(item);
        }
    }

    private void removeWarningCode1xx(HeaderValueWarning warning) {
        ArrayList<WarningValue> removeWarningValue = new ArrayList<WarningValue>();
        for (WarningValue warningValue : warning.warningValueList()) {
            if (warningValue.code().is1xx()) {
                removeWarningValue.add(warningValue);
            }
        }

        for (WarningValue warningValue : removeWarningValue) {
            warning.warningValueList().remove(warningValue);
        }
    }

    private void moveResponseHeader(Response response) {
        int count = response.headerCount();
        for (int index = 0; index < count; index++) {
            HeaderItem item = response.getHeaderItem(index);
            ResponseSetter.setHeader(this.response, item.sort(), item.valueBytes());
        }
    }

    private void setForUpdate(Request request, Response response) {
        requestMethod = request.method();
        setVaryHeaderAndSelectionFields(request, response);

        etag = getETagHeaderValue(response);
        date = getDateHeaderValue(response);
        age = getAgeHeaderValue(response);

        requestTime = request.requestTime();
        responseTime = response.responseTime();
        freshnessLifeTime = calculateFreshnessLifeTime(response);
    }

    public synchronized void invalidate() {
        if (timerEventForRemove != null) {
            entry.manager().timer().removeEvent(timerEventForRemove);
            timerEventForRemove = null;
        }
        entry.manager().sizeLimiter().subtractSize(getStorageSize());

        StoredResponseStorer.deleteTempFile(this, true);
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

        timerEventForRemove = null;
        usingCount = 0;
    }

    public boolean isMatchMethodAndSelectedFields(Request request) {
        if (request.method() == requestMethod
                && isMatchSelectedFields(request)) {
            return true;
        }
        return false;
    }

    private boolean isMatchSelectedFields(Request request) {
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

    public boolean isFresh(Request request) {
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

    public long currentAge() {
        long apparentAge = Math.max(0, responseTime - date);
        long responseDelay = responseTime - requestTime;
        long correctedAgeValue = age * 1000 + responseDelay;
        long correctedInitalAge = Math.max(apparentAge, correctedAgeValue);
        long residentTime = now() - responseTime;
        long currentAge = (correctedInitalAge + residentTime) / 1000;
        return currentAge;
    }

    private long now() {
        return new Date().getTime();
    }

    public void replace(Request request, Response response) {
        StoredResponseStorer.deleteTempFile(this, true);
        set(request, response, null);
        StoredResponseStorer.store(this);
    }

    public void startTimerForCheckingInactive() {
        timerEventForRemove = entry.manager().timer().addEvent(entry.manager().inactiveTime(), entry, this);
    }

    public synchronized void lockUsing() {
        usingCount++;

        if (timerEventForRemove != null) {
            entry.manager().timer().removeEvent(timerEventForRemove);
            timerEventForRemove = null;
        }
    }

    public synchronized void freeUsing() {
        usingCount--;
        timerEventForRemove = entry.manager().timer().addEvent(entry.manager().inactiveTime(), entry, this);
    }

    public synchronized int usingCount() {
        return usingCount;
    }

    public long getStorageSize() {
        return infoFile.length() + responseFile.length() + response.contentLength();
    }

    public void entry(CacheEntry entry) {
        this.entry = entry;
    }

    public MethodType requestMethod() {
        return requestMethod;
    }

    public void requestMethod(MethodType requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Set<HeaderValue> selectionFields() {
        return selectionFields;
    }

    public byte[] etag() {
        return etag;
    }

    public void etag(byte[] etag) {
        this.etag = etag;
    }

    public long date() {
        return date;
    }

    public void date(long date) {
        this.date = date;
    }

    public long age() {
        return age;
    }

    public void age(long age) {
        this.age = age;
    }

    public long requestTime() {
        return requestTime;
    }

    public void requestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public long responseTime() {
        return responseTime;
    }

    public void responseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long freshnessLifeTime() {
        return freshnessLifeTime;
    }

    public void freshnessLifeTime(long freshnessLifeTime) {
        this.freshnessLifeTime = freshnessLifeTime;
    }

    public File parentPathFile() {
        return entry.directoryFile();
    }

    public File infoFile() {
        return infoFile;
    }

    public void infoFile(File infoFile) {
        this.infoFile = infoFile;
    }

    public File responseFile() {
        return responseFile;
    }

    public void responseFile(File responseFile) {
        this.responseFile = responseFile;
    }

    public File bodyFile() {
        return bodyFile;
    }

    public void bodyFile(File bodyFile) {
        this.bodyFile = bodyFile;
    }

    public Response response() {
        return response;
    }

    public void response(Response response) {
        this.response = response;
    }

    public void addTotalSize() {
        entry.manager().sizeLimiter().addSize(getStorageSize());
    }
}
