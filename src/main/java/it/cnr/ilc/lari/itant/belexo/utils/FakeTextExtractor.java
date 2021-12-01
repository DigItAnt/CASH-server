package it.cnr.ilc.lari.itant.belexo.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
generate a list of differentChars * (nrep * char)
es. aajjff, hhkkee
*/
public class FakeTextExtractor implements TextExtractorInterface {
    static char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    int maxTokens = 10;
    int differentChars = 5;
    int nRep = 3;

    List<String> tokenList;


    public FakeTextExtractor() {
    }

    @Override
    public TextExtractorInterface read(InputStream content) {
        this.tokenList = generateTokens();
        return this;
    }

    public FakeTextExtractor(int maxtokens) {
        this.maxTokens = maxtokens;
    }

    private List<String> generateTokens() {
        List<String> toret = new ArrayList<>();
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
            toret.add(new String(token));
        }
        return toret;
    }

    @Override
    public List<String> tokens() {
        return this.tokenList;
    }

    @Override
    public String extract() {
        return String.join(" ", this.tokenList);
    }

    public static void main(String[] args) {
        for (String token: new FakeTextExtractor().read(null).tokens() )
            System.out.println(token);
    }
}
