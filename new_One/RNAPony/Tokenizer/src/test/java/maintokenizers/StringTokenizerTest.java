package maintokenizers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringTokenizerTest {

    StringTokenizer tokenizer;

    @Test
    void checkDelimsChoosing(){
        tokenizer = new StringTokenizer(" \f\njestem \n\t  \f\njestem \n\t s t r", "\n");
        String msg = "Incorrect first token when choosing special delimiter";
        assertEquals(" \f", tokenizer.nextToken(), msg);
        assertEquals("jestem ", tokenizer.nextToken(), msg);
        assertEquals("\t  \f", tokenizer.nextToken(), msg);
        assertEquals("jestem ", tokenizer.nextToken(), msg);
        assertEquals("\t s t r", tokenizer.nextToken(), msg);
        tokenizer = new StringTokenizer(" \f\njestem \n\t  \f\njestem \n\t s t r", "\f\t");
        assertEquals(" ", tokenizer.nextToken(), msg);
        assertEquals("\njestem \n", tokenizer.nextToken(), msg);
        assertEquals("  ", tokenizer.nextToken(), msg);
        assertEquals("\njestem \n", tokenizer.nextToken(), msg);
        assertEquals(" s t r", tokenizer.nextToken(), msg);
    }

    @Test
    void countTokens() {
        tokenizer = new StringTokenizer(" \f\njestem \n\t  \f\njestem \n\t s t r");
        String msg = "Didn't count correct number of tokens when text end with token";
        assertEquals(5, tokenizer.countTokens(), msg);
        tokenizer = new StringTokenizer(" \f\njestem \n\t  \f\njestem \n\t s t r\r");
        msg = "Didn't count correct number of tokens when text end with delimiter";
        assertEquals(5, tokenizer.countTokens(), msg);
        tokenizer.nextToken();//jestem
        msg = "Didn't count correct number of all tokens when found one token";
        assertEquals(5, tokenizer.countTokens(), msg);
    }

    @Test
    void hasMoreTokens() {
        tokenizer = new StringTokenizer(" \f\njestem \n\t  \f\njestem \n\t s t r");
        assertTrue(tokenizer.hasMoreTokens(), "Didn't see tokens before finding first");
        tokenizer.nextToken();
        assertTrue(tokenizer.hasMoreTokens(), "Didn't see tokens after finding first");
        tokenizer.nextToken();
        tokenizer.nextToken();
        assertTrue(tokenizer.hasMoreTokens(), "Didn't see tokens after finding n tokens");
        tokenizer.nextToken();
        assertTrue(tokenizer.hasMoreTokens(), "Didn't see last token when it is last character in text");
        tokenizer.nextToken();
        assertFalse(tokenizer.hasMoreTokens(), "See tokens, when there is no more tokens");
        tokenizer = new StringTokenizer(" \f\njestem \n\t  \f\njestem \n\t s t r\n");
        tokenizer.nextToken();
        tokenizer.nextToken();
        tokenizer.nextToken();
        tokenizer.nextToken();
        tokenizer.nextToken();
        assertFalse(tokenizer.hasMoreTokens(), "See tokens, when there is no more tokens, but delimiter at the end");
    }

    @Test
    void nextToken() {
        tokenizer = new StringTokenizer(" \f\njestem \n\t  \f\njestem \n\t s t r");
        String msg = "Incorrect first token";
        assertEquals("jestem", tokenizer.nextToken(), msg);
        tokenizer.nextToken();//jestem
        tokenizer.nextToken();//s
        tokenizer.nextToken();//t
        msg = "Incorrect last token";
        assertEquals("r", tokenizer.nextToken(), msg);
        tokenizer = new StringTokenizer(" \f\njestem \n\t  \f\njestem \n\t s t r\r");
        tokenizer.nextToken();//jestem
        tokenizer.nextToken();//jestem
        tokenizer.nextToken();//s
        tokenizer.nextToken();//t
        msg = "Incorrect last token, when there is delimiter at the end";
        assertEquals("r", tokenizer.nextToken(), msg);
    }

    @Test
    void reloadText() {
        tokenizer = new StringTokenizer(" \f\njestem \n\t  \f\njestem \n\t s t r");
        tokenizer.nextToken();//jestem
        tokenizer.nextToken();//s
        tokenizer.reloadText(" \f\ninne \n\t  \f\ninne2 \n\t a b c\r");
        String msg = "Wrong token after reloading text with tokens";
        assertEquals("inne", tokenizer.nextToken(), msg);
    }
}