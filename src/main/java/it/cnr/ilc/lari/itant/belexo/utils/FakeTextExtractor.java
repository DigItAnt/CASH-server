package it.cnr.ilc.lari.itant.belexo.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import it.cnr.ilc.lari.itant.belexo.om.Annotation;

/*
generate a list of differentChars * (nrep * char)
es. aajjff, hhkkee
*/
public class FakeTextExtractor implements TextExtractorInterface {
    static char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    int maxTokens = 10;
    int differentChars = 5;
    int nRep = 3;

    List<TokenInfo> tokenList;

    public FakeTextExtractor() {
    }

    public String getTextType() { return "fake"; }

    @Override
    public Map<String, Object> metadata() { return null; }

    @Override
    public TextExtractorInterface read(InputStream content) {
        this.tokenList = generateTokens();
        return this;
    }

    @Override
    public List<Annotation> annotations() {
        return new ArrayList<Annotation>();
    }

    public FakeTextExtractor(int maxtokens) {
        this.maxTokens = maxtokens;
    }

    private List<TokenInfo> generateTokens() {
        List<TokenInfo> toret = new ArrayList<>();
        int numTokens = new Random().nextInt(maxTokens);
        char[] token = new char[nRep * differentChars];
        for (int i= 0; i < numTokens; i++) {
            for (int j = 0; j < differentChars; j++) {
                //char c = alphabet[new Random().nextInt(alphabet.length)];
                char c = alphabet[new Random().nextInt(10)];
                for (int k = 0; k < nRep; k++) {
                    token[j*nRep+k] = c;
                }
            }
            toret.add(new TokenInfo(new String(token)));
        }
        return toret;
    }

    @Override
    public List<TokenInfo> tokens() {
        return this.tokenList;
    }

    @Override
    public String extract() {
        return String.join(" ", TokenInfo.allWordTokens(tokenList));
    }

    public static void main(String[] args) {
        for (TokenInfo token: new FakeTextExtractor().read(null).tokens() )
            System.out.println(token.text);
    }
}
