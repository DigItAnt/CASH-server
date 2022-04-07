package it.cnr.ilc.lari.itant.cash.om;

import java.util.List;

public class GetAnnotationsResponse extends ReqUUIDResponse {
    List<Annotation> annotations;

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }
        
}
