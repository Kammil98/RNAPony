package mainTokenizers;

import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringTokenizer {
    private String text;
    private long count_;
    private final Matcher tokenMatcher;
    private Deque<MatchResult> tokens;

    //strToNum function is replaced by Wrapper.parsePrimitive(str)
    //for example Integer.parseInt(str)

    /**
     * Initialize StringTokenizer class, to extract Tokens
     * @param text text with tokens to extract
     * @param delims possible delimiters in text with tokens
     */
    public StringTokenizer(String text, String delims){
        this.text = text;
        Pattern delimsPattern = Pattern.compile("[^" + delims + "]+");
        this.tokenMatcher = delimsPattern.matcher(this.text);
        this.tokens = this.tokenMatcher.results().collect(Collectors.toCollection(LinkedList::new));
        this.count_ = this.tokens.size();
    }

    /**
     * Initialize StringTokenizer class, to extract Tokens
     * with default delimiters : " \f\n\r\t"
     * @param text text with tokens to extract
     */
    public StringTokenizer(String text){
        this(text, " \f\n\r\t");
    }

    /**
     * @return amount of tokens in whole text
     */
    public long countTokens(){
        return count_;
    }

    /**
     * @return true, if there is more tokens,
     * which wasn't taken from StringTokenizer.
     * false otherwise
     */
    public boolean hasMoreTokens(){
        return !tokens.isEmpty();
    }

    /**
     * poll next token from FIFO
     * @return next token from text or null if there is no more tokens
     */
    public String nextToken(){
        return tokens.isEmpty() ? null : tokens.pollFirst().group();
    }

    /**
     * Extract tokens from new text with the same delimiters
     * @param text text with tokens to extract
     */
    public void reloadText(String text){
        this.text = text;
        tokenMatcher.reset(this.text);
        tokens = tokenMatcher.results().collect(Collectors.toCollection(LinkedList::new));
        count_ = tokens.size();
    }
}
