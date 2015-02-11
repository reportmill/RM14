/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.parse;
import java.util.*;
import snap.parse.ParseRule.*;

/**
 * A Parser to parse simple snap grammar rule files.
 */
public class ParseRuleParser extends Parser {

    ParseRule number = new ParseRule("Number");
    ParseRule string = new ParseRule("String");
    ParseRule name = new ParseRule("Name");
    ParseRule primaryExpr = new ParseRule("PrimaryExpr");
    ParseRule expression = new ParseRule("Expression");

/**
 * Creates a new ParseRule rule.
 */
public ParseRule createRule()
{
    // Number, String, Name
    number.setPattern("[1-9][0-9]*");
    string.setPattern("\"(([^\"\\\\\\n\\r])|(\\\\.))*\"");
    name.setPattern("[$a-zA-Z][$\\w]*");
    
    // PrimaryExpr
    primaryExpr.or(string);
    primaryExpr.or("LookAhead").and("(").and(new ParseRule().or(number).or(expression)).and(")");
    primaryExpr.or(name);
    primaryExpr.or("(").and(expression).and(")");
    
    // CountExpr
    ParseRule countExpr = new ParseRule("CountExpr");
    countExpr.or(primaryExpr).and(new ParseRule().or("*").or("+").or("?"), '?');
    
    // AndExpr
    ParseRule andExpr = new ParseRule("AndExpr").or(countExpr).and(countExpr, '*');
    
    // Configure Expression
    expression.or(andExpr).and(new ParseRule().or("|").and(andExpr), '*');
    
    // Create ParseRule rule
    ParseRule prrule = new ParseRule("ParseRule");
    prrule.or(name).and("{").and(expression).and("}");
    
    // Create ParseRule file rule
    ParseRule prfile = new ParseRule("ParseRuleFile"); prfile._op = Op.ZeroOrMore; prfile._child0 = prrule;
    prfile.or(prrule, '*');
    
    // Set handlers and return file rule
    primaryExpr.setHandler(new PrimaryExprHandler());
    countExpr.setHandler(new CountExprHandler());
    andExpr.setHandler(new AndExprHandler());
    expression.setHandler(new ExpressionHandler());
    prrule.setHandler(new ParseRuleHandler());
    prfile.setHandler(new ParseRuleFileHandler());
    return prfile;
}

/**
 * Override to allow rules files to have standard Java single/multiple line comments.
 */
protected Tokenizer createTokenizer()
{
    Tokenizer tz = super.createTokenizer();
    tz.setReadSingleLineComments(true); tz.setReadMultiLineComments(true);
    return tz;
}

/**
 * Returns a named rule.
 */
private static ParseRule getRule2(String aName)
{
    ParseRule rule = _rules.get(aName);
    if(rule==null)
        _rules.put(aName, rule=new ParseRule(aName));
    return rule;
} static Map <String,ParseRule> _rules = new HashMap();

/**
 * A Handler for PrimaryExpr.
 */
private static class PrimaryExprHandler extends ParseHandler <ParseRule> {

    /** Called when node is parsed. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Name
        if(anId=="Name")
            _part = getRule2(aNode.getString());
        
        // Handle string
        else if(anId=="String") {
            String string = aNode.getString(), pattern = string.substring(1, string.length()-1);
            getPart().setPattern(pattern);
        }
        
        // Handle Expression
        else if(anId=="Expression") {
            ParseRule rule = aNode.getCustomNode(ParseRule.class);
            if(_part==null) _part = rule;
            else _part._child0 = rule;  // LookAhead
        }
        
        // Handle LookAhead
        else if(aNode.getPattern()=="LookAhead")
            getPart().setLookAhead(99);
        
        // Handle Number
        else if(anId=="Number") {
            String str = aNode.getString(); int count = Integer.valueOf(str);
            getPart().setLookAhead(count);
        }
    }
}

/**
 * A Handler for CountExpr.
 */
private static class CountExprHandler extends ParseHandler <ParseRule> {

    /** Called when node is parsed. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle PrimaryExpr
        if(anId=="PrimaryExpr")
            _part = aNode.getCustomNode(ParseRule.class);
        
        // Handle Counts
        else if(anId=="*") _part = new ParseRule(Op.ZeroOrMore, _part);
        else if(anId=="+") _part = new ParseRule(Op.OneOrMore, _part);
        else if(anId=="?") _part = new ParseRule(Op.ZeroOrOne, _part);
    }
}

/**
 * A Handler for AndExpr.
 */
private static class AndExprHandler extends ParseHandler <ParseRule> {

    ParseRule _more;
    
    /** Called when node is parsed. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle CountExpr
        if(anId=="CountExpr") {
            ParseRule rule = aNode.getCustomNode(ParseRule.class);
            if(_part==null) { _part = rule; _more = null; }
            else if(_more==null) _part = _more = new ParseRule(Op.And, _part, rule);
            else { _more._child1 = new ParseRule(Op.And, _more._child1, rule); _more = _more._child1; }
        }
    }
}

/**
 * A Handler for Expression.
 */
private static class ExpressionHandler extends ParseHandler <ParseRule> {

    ParseRule _more;

    /** Called when node is parsed. */
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle AndExpr
        if(anId=="AndExpr") {
            ParseRule rule = aNode.getCustomNode(ParseRule.class);
            if(_part==null) { _part = rule; _more = null; }
            else if(_more==null) _part = _more = new ParseRule(Op.Or, _part, rule);
            else { _more._child1 = new ParseRule(Op.Or, _more._child1, rule); _more = _more._child1; }
        }
    }
}

/**
 * A Handler for ParseRule.
 */
private static class ParseRuleHandler extends ParseHandler <ParseRule> {

    // Called when node is parsed.
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Name
        if(anId=="Name")
            _part = getRule2(aNode.getString());
        
        // Handle Expression
        if(anId=="Expression") {
            ParseRule rule = aNode.getCustomNode(ParseRule.class);
            _part._op = rule._op;
            _part._child0 = rule._child0; _part._child1 = rule._child1;
            _part._pattern = rule._pattern; _part._literal = rule._literal;
            _part._lookAhead = rule._lookAhead;
        }
    }
}

/**
 * A Handler for ParseRuleFile.
 */
private static class ParseRuleFileHandler extends ParseHandler <ParseRule> {

    // Called when node is parsed.
    protected void parsedOne(ParseNode aNode, String anId)
    {
        // Get first
        if(_part==null) {
            ParseRule rule = aNode.getCustomNode(ParseRule.class);
            if(rule.getPattern()==null)
                _part = rule;
        }
    }
    
    /** Override to reset Rules map. */
    public ParseRule parsedAll()  { _rules = new HashMap(); return super.parsedAll(); }
}
        
}