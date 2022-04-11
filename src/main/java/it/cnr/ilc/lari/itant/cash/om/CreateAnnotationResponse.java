package it.cnr.ilc.lari.itant.cash.om;

public class CreateAnnotationResponse extends ReqUUIDResponse {
    Annotation annotation;

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }
    
}
