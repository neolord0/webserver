package kr.dogfoot.webserver.server.resource.negotiation;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.server.resource.ResourceDirectory;

import java.io.File;
import java.util.ArrayList;

public class NegotiationInfo {
    private static final HeaderSort[] Zero_Array = new HeaderSort[0];
    private static final NegotiationVariant[] Zero_Array2 = new NegotiationVariant[0];
    private String filename;

    private ArrayList<HeaderSort> compareOrder;
    private ArrayList<NegotiationVariant> variantList;

    public NegotiationInfo() {
        variantList = new ArrayList<NegotiationVariant>();
        compareOrder = new ArrayList<HeaderSort>();
    }

    public void parse(ResourceDirectory parentDirectory, File file) {
        NegotiationFileParser.parse(this, parentDirectory, file);
    }


    public String filename() {
        return filename;
    }

    public void filename(String filename) {
        this.filename = filename;
    }

    public void addCompareHeader(HeaderSort headerSort) {
        compareOrder.add(headerSort);
    }

    public HeaderSort[] compareHeaders() {
        return compareOrder.toArray(Zero_Array);
    }

    public void addVariant(NegotiationVariant variant) {
        variantList.add(variant);
    }

    public NegotiationVariant[] variants() {
        return variantList.toArray(Zero_Array2);
    }
}
