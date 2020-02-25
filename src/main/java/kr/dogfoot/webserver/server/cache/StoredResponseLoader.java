package kr.dogfoot.webserver.server.cache;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.FactoryForHeaderValue;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.httpMessage.response.Response;
import kr.dogfoot.webserver.httpMessage.response.StatusCode;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.performer.util.ContentRange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class StoredResponseLoader {
    public static StoredResponse load(File infoFile, CacheEntry entry) {
        if (infoFile.exists() == false) {
            return null;
        }
        StoredResponse sr = StoredResponse.pooledObject();
        sr.entry(entry);

        setFile(sr, infoFile);
        loadReponse(sr);
        if (sr.response() != null) {
            loadInfo(sr);
        }
        setVaryHeader(sr);
        return sr;
    }

    private static void setFile(StoredResponse sr, File infoFile) {
        sr.infoFile(infoFile);
        String infoFileName = sr.infoFile().getName();
        sr.responseFile(new File(sr.parentPathFile(), infoFileName.substring(0, infoFileName.length() - 4) + ".rsp"));
        sr.bodyFile(new File(sr.parentPathFile(), infoFileName.substring(0, infoFileName.length() - 4) + ".bdy"));
    }

    private static void loadReponse(StoredResponse sr) {
        try (FileInputStream fis = new FileInputStream(sr.responseFile())) {
            try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                Response response = new Response();
                response.majorVersion((Short) ois.readObject());
                response.minorVersion((Short) ois.readObject());
                response.statusCode((StatusCode) ois.readObject());
                int headerCount = (int) ois.readObject();
                for (int index = 0; index < headerCount; index++) {
                    HeaderSort sort = (HeaderSort) ois.readObject();
                    byte[] value = (byte[]) ois.readObject();

                    response.addHeader(sort, value);
                }

                sr.response(response);
                setResponseRange(sr);

                ois.close();
                fis.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            sr.response(null);
        }
    }

    private static void setResponseRange(StoredResponse sr) {
        if (sr.response().range() == null) {
            sr.response().range(new ContentRange());
        }
        sr.response().range().lastPos(sr.bodyFile().length() - 1);
    }


    private static void loadInfo(StoredResponse sr) {
        try (FileInputStream fis = new FileInputStream(sr.infoFile())) {
            try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                sr.requestMethod((MethodType) ois.readObject());
                int selectFieldCount = (int) ois.readObject();
                for (int index = 0; index < selectFieldCount; index++) {
                    HeaderSort sort = (HeaderSort) ois.readObject();
                    byte[] value = (byte[]) ois.readObject();

                    HeaderValue headerValue = FactoryForHeaderValue.create(sort);
                    try {
                        headerValue.parseValue(value);
                        sr.addSelectField(headerValue);
                    } catch (ParserException e) {
                        headerValue = null;
                    }
                }

                sr.etag((byte[]) ois.readObject());
                sr.date((Long) ois.readObject());
                sr.age((Long) ois.readObject());
                sr.requestTime((Long) ois.readObject());
                sr.responseTime((Long) ois.readObject());
                sr.freshnessLifeTime((Long) ois.readObject());

                ois.close();
                fis.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void setVaryHeader(StoredResponse sr) {
        sr.setVaryHeader(sr.response());
    }
}
