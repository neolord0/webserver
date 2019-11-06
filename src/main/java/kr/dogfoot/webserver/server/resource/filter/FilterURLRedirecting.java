package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.reply.ReplyCode;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.util.http.HttpString;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

public class FilterURLRedirecting extends Filter {
    private String restSourceURLPattern = "";
    private ReplyCode replyCode;
    private String targetURL = "";

    public FilterURLRedirecting() {
        restSourceURLPattern = "";
        replyCode = ReplyCode.Code301;
        targetURL = "";
    }

    @Override
    public boolean inboundProcess(Context context, Server server) {
        String sourceURLPattern = getSourceURLPattern();
        if (isMatchURL(context.request().requestURI().toString(), sourceURLPattern)) {
            context.reply(server.objects().replyMaker().new_RedirectReply(replyCode, targetURL));
            return false;
        }
        return true;
    }

    private boolean isMatchURL(String url, String urlPattern) {
        return FilenameUtils.wildcardMatch(url, urlPattern, IOCase.INSENSITIVE);
    }

    private String getSourceURLPattern() {
        String resourceURL = resource().pathFromRoot();
        boolean endWith = resourceURL.endsWith(HttpString.Slash_String);
        boolean startWith = restSourceURLPattern.startsWith(HttpString.Slash_String);

        if (endWith == true && startWith == true) {
            return resourceURL + restSourceURLPattern.substring(1);
        } else if (endWith == true || startWith == true) {
            return resourceURL + restSourceURLPattern;
        } else {
            return resourceURL + HttpString.Slash_String + restSourceURLPattern;
        }
    }

    @Override
    public boolean outboundProcess(Context context, Server server) {
        return true;
    }

    @Override
    public FilterSort sort() {
        return FilterSort.URLRedirecting;
    }

    public String restSourceURLPattern() {
        return restSourceURLPattern;
    }

    public void restSourceURLPattern(String restSourceURLPattern) {
        this.restSourceURLPattern = restSourceURLPattern;
    }

    public ReplyCode replyCode() {
        return replyCode;
    }

    public void replyCode(ReplyCode replyCode) {
        this.replyCode = replyCode;
    }

    public String targetURL() {
        return targetURL;
    }

    public void targetURL(String targetURL) {
        this.targetURL = targetURL;
    }
}
