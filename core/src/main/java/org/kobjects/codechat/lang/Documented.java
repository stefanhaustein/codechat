package org.kobjects.codechat.lang;

import java.util.List;

public interface Documented {

    String getDocumentation(List<AnnotationSpan> links);

}
