package it.cnr.ilc.lari.itant.cash.utils;

public class StringUtils {
    public static String n(int n, String element, String separator) {
        StringBuffer ret = new StringBuffer("");
        while ( n-- > 0 ) {
            ret.append(element);
            if ( n > 0 ) ret.append(separator);
        }
        return ret.toString();
    }

    public static String sqlEscapeString(String str){
        String data = null;
        if (str != null) {
          str = str.replace("\\", "\\\\");
          str = str.replace("'", "\\'");
          str = str.replace("\0", "\\0");
          str = str.replace("\n", "\\n");
          str = str.replace("\r", "\\r");
          str = str.replace("\"", "\\\"");
          str = str.replace("\\x1a", "\\Z");
          data = str;
        }
        return data;
      }
}
