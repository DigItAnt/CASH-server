package it.cnr.ilc.lari.itant.belexo.utils;

public class StringUtils {
    public static String n(int n, String element, String separator) {
        StringBuffer ret = new StringBuffer("");
        while ( n-- > 0 ) {
            ret.append(element);
            if ( n > 0 ) ret.append(separator);
        }
        return ret.toString();
    }
}
