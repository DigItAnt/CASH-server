package it.cnr.ilc.lari.itant.cash.om;

import java.util.List;

public class GetAnnotationResponse extends ReqUUIDResponse {
    Annotation annotation;

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }
        
}
