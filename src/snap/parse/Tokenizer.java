/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import java.util.*;
import java.util.regex.*;
import snap.util.ListUtils;

/**
 * A class to extract tokens from a char sequence.
 */
public class Tokenizer {

    // The tokenizer input
    CharSequence     _input;
    
    // Whether to support standard Java single line comments and multiple line comments
    boolean          _slc, _mlc, _jc;
    
    // The current char index
    protected int    _charIndex;
    
    // The current line index and line start char index
    protected int    _lineIndex, _lineStart;
    
    // The list of regular expression objects
    List <Regex>     _regexList = new ArrayList();
    
    // An array of regexes
    Regex            _regexes[];

    // A map of char to matchers
    Regex            _charMatchers[][] = new Regex[128][];

/**
 * Returns the current tokenizer input.
 */
public CharSequence getInput()  { return _input; }

/**
 * Sets the current tokenizer input.
 */
public void setInput(CharSequence anInput)
{
    _input = anInput; _charIndex = _lineIndex = _lineStart = 0;
    for(Regex regex : _regexList)
        regex.getMatcher(_input).reset(_input);
}

/**
 * Returns the input subsequence for the given range of characters in input.
 */
public CharSequence getInput(int aStart, int anEnd)  { return _input.subSequence(aStart, anEnd); }

/**
 * Returns whether tokenizer reads standard Java single line comments.
 */
public boolean isReadSingleLineComments()  { return _slc; }

/**
 * Sets whether tokenizer reads standard Java single line comments.
 */
public void setReadSingleLineComments(boolean aValue)  { _slc = aValue; _jc = _slc || _mlc; }

/**
 * Returns whether tokenizer reads standard Java multiple line comments.
 */
public boolean isReadMultiLineComments()  { return _mlc; }

/**
 * Sets whether tokenizer reads standard Java multiple line comments.
 */
public void setReadMultiLineComments(boolean aValue)  { _mlc = aValue; _jc = _slc || _mlc; }

/**
 * Adds a pattern.
 */
public void addPattern(String aName, String aPattern, boolean isLiteral)
{
    // Get unique pattern string
    String pattern = aPattern.intern();
    
    // If pattern already in list, just return
    for(Regex regex : _regexList)
        if(regex.getPattern()==pattern)
            return;

    // Create and add new regex
    _regexList.add(new Regex(aName, pattern, isLiteral));
    _regexes = null;
}

/**
 * Adds patterns to this tokenizer for given rule.
 */
public void addPatterns(ParseRule aRule)  { addPatterns(aRule, new ArrayList(128)); }

/**
 * Adds patterns to this tokenizer for given rule.
 */
private void addPatterns(ParseRule aRule, List theRules)
{
    theRules.add(aRule);
    if(aRule.getPattern()!=null)
        addPattern(aRule.getName(), aRule.getPattern(), aRule.isLiteral());
    ParseRule r0 = aRule.getChild0(); if(r0!=null && !ListUtils.containsId(theRules, r0)) addPatterns(r0, theRules);
    ParseRule r1 = aRule.getChild1(); if(r1!=null && !ListUtils.containsId(theRules, r1)) addPatterns(r1, theRules);
}

/**
 * Returns the array of regexes (creating it if missing).
 */
protected Regex[] getRegexes()  { return _regexes!=null? _regexes : (_regexes=_regexList.toArray(new Regex[0])); }

/**
 * CharSequence method.
 */
public final char charAt(int anIndex)  { return _input.charAt(anIndex); }

/**
 * CharSequence method.
 */
public final int length()  { return _input.length(); }

/**
 * Returns the current parse char.
 */
public final char getChar()  { return _input.charAt(_charIndex); }

/**
 * Returns the current parse char location.
 */
public final int getCharIndex()  { return _charIndex; }

/**
 * Returns the current line index.
 */
public final int getLineIndex()  { return _lineIndex; }

/**
 * Returns the current line start index.
 */
public final int getLineStart()  { return _lineStart; }

/**
 * Returns the current column index in the current line.
 */
public final int getColumnIndex()  { return getCharIndex() - getLineStart(); }

/**
 * Returns the current line number.
 */
public final int getLineNumber()  { return getLineIndex()+1; }

/**
 * Returns the current column number.
 */
public final int getColumnNumber()  { return getCharIndex() - getColumnIndex() + 1; }

/**
 * Returns the next token.
 */
public Token getNextToken()
{
    // Get next special token
    Token specialToken = getNextSpecialToken();
    
    // Get list of matchers for next char
    char c = _charIndex<length()? getChar() : 0;
    Regex regexes[] = c<128? getRegexes(c) : getRegexes();
    
    // Iterate over regular expressions to find best match
    Regex match = null; int matchEnd = _charIndex;
    for(Regex regex : regexes) {
        
        // Get matcher
        Matcher matcher = regex.getMatcher(_input);
        matcher.region(_charIndex, _input.length());
    
        // Find pattern
        if(matcher.lookingAt()) {
            if(match==null || matcher.end()>matchEnd ||
                (matcher.end()==matchEnd && regex.getLiteralLength()>match.getLiteralLength())) {
                match = regex;
                matchEnd = matcher.end();
            }
        }
    }
    
    // If no match, return null
    if(match==null) {
        if(_charIndex==length()) return null;
        throw new ParseException("Token not found for: " + getInput(_charIndex, Math.min(_charIndex+30,length())));
    }
    
    // Create new token, reset end and return new token
    Token token = createToken(match.getName(), match.getPattern(), _charIndex, matchEnd, specialToken);
    _charIndex = matchEnd;
    return token;
}

/**
 * Returns list of Regex for a char.
 */
public Regex[] getRegexes(char aChar)
{
    // Get cached regex array for char
    Regex regexes[] = _charMatchers[aChar];
    
    // If not found, create array by checking first char of all regexes and set
    if(regexes==null) {
        if(aChar==0) return _charMatchers[aChar] = new Regex[0];
        List <Regex> regexList = new ArrayList(); String str = Character.toString(aChar);
        for(Regex regex : getRegexes()) { char c = regex.getLiteralChar();
            if(c==aChar)
                regexList.add(regex);
            else if(c==0) { // Check "char.startsWith(regex)"
                Pattern p = regex.getPatternCompiled(); Matcher m = p.matcher(str); m.matches();
                if(m.hitEnd())
                    regexList.add(regex);
            }
        }
        regexes = _charMatchers[aChar] = regexList.toArray(new Regex[0]);
    }
    
    // Return regexes array
    return regexes;
}

/**
 * Creates a new token.
 */
protected Token createToken(String aName, String aPattern, int aStart, int anEnd, Token aSpclTkn)
{
    Token.BasicToken token = new Token.BasicToken();
    token._tokenizer = this;
    token._name = aName; token._pattern = aPattern;
    token._start = aStart; token._end = anEnd;
    token._lineIndex = _lineIndex; token._lineStart = _lineStart;
    token._specialToken = aSpclTkn;
    return token;
}

/**
 * Processes and returns next special token.
 */
public Token getNextSpecialToken()
{
    Token spt = null, sptn = getNextSpecialToken(null);
    while(sptn!=null) { spt = sptn; sptn = getNextSpecialToken(sptn); }
    return spt;
}

/**
 * Processes and returns next special token.
 */
protected Token getNextSpecialToken(Token aSpclTkn)
{
    // Gobble whitespace
    goobleWhiteSpace();
    
    // Look for standard Java single/multi line comments tokens
    if(!_jc || _charIndex+1>=length() || getChar()!='/') return null;
    Token token = _slc? getSingleLineCommentToken(aSpclTkn) : null;
    if(token==null && _mlc) token = getMultiLineCommentToken(aSpclTkn);

    // Return token
    return token;
}

/**
 * Gobble input characters until next non-whitespace or input end.
 */
protected void goobleWhiteSpace()
{
    char c; while(_charIndex<length() && Character.isWhitespace(c=getChar())) { _charIndex++;
        if(c=='\n' || c=='\r') {
            if(c=='\r' && _charIndex<length() && getChar()=='\n') _charIndex++;
            _lineIndex++; _lineStart = _charIndex;
        }
    }
}

/**
 * Processes and returns a single line comment token if next up in input.
 */
protected Token getSingleLineCommentToken(Token aSpclTkn)
{
    // If next two chars are single line comment (//), return token
    if(_charIndex+1<length() && getChar()=='/' && charAt(_charIndex+1)=='/') {
        int start = _charIndex; _charIndex += 2;
        _charIndex = indexAfterNewline(getInput(), _charIndex);
        if(_charIndex<0) _charIndex = length(); else { _lineIndex++; _lineStart = _charIndex; }
        return createToken("SingleLineComment", null, start, _charIndex, aSpclTkn);
    }
    
    // Return null since not found
    return null;
}

/**
 * Process and return a multi-line comment if next up in input.
 */
protected Token getMultiLineCommentToken(Token aSpclTkn)
{
    // If next two chars are multi line comment (/*) prefix, return token
    if(_charIndex+1<length() && getChar()=='/' && charAt(_charIndex+1)=='*')
        return getMultiLineCommentTokenMore(aSpclTkn);
    return null;
}

/**
 * Returns a token from the current char to multi-line comment termination or input end.
 */
protected Token getMultiLineCommentTokenMore(Token aSpclTkn)
{
    // Mark start of MultiLineComment token (just return null if at input end)
    int start = _charIndex; if(start==length()) return null;
    
    // Gobble chars until multi-line comment termination or input end
    while(_charIndex<length()) {
        char c = charAt(_charIndex++);
        if(c=='*' && _charIndex<length() && charAt(_charIndex)=='/') { _charIndex++; break; }
        if(c=='\n' || c=='\r') {
            if(c=='\r' && _charIndex<length() && charAt(_charIndex)=='\n') _charIndex++;
            _lineIndex++; _lineStart = _charIndex;
        }
    }
    
    // Create and return token
    return createToken("MultiLineComment", null, start, _charIndex, aSpclTkn);
}

/**
 * Returns the index just beyond next newline (or carriage-return/newline) in given char sequence starting at given
 * char index. 
 */
private static int indexAfterNewline(CharSequence aSequence, int aStart)
{
    for(int i=aStart, iMax=aSequence.length(); i<iMax; i++) { char c = aSequence.charAt(i);
        if(c=='\r')
            return i+1<iMax && aSequence.charAt(i+1)=='\n'? (i+2) : (i+1);
        if(c=='\n')
            return i+1;
    }
    return -1;
}

}