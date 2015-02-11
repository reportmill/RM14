package snap.util;
import com.reportmill.base.RMKeyChain;
import com.reportmill.base.RMKeyChain.*;
import snap.parse.*;

/**
 * A Parser subclass to parse strings to KeyChains.
 */
public class KeyChainParser extends Parser {

    // The error from last parse
    String    _error;

/**
 * Returns a KeyChain for given string.
 */
public RMKeyChain keyChain(String aString)
{
    // Parse string
    try { return parse(aString).getCustomNode(RMKeyChain.class); }
    catch(Throwable t) { _error = "Error parsing keychain: @" + aString + "@\n" + t.getMessage(); }
    return new RMKeyChain(RMKeyChain.Op.Literal);
}

/**
 * Returns the error from last key chain parse.
 */
public String getError() { return _error; }

/**
 * Returns the error from last KeyChain parse and clears error.
 */
public String getAndResetError() { String e = _error; _error = null; return e; }

/**
 * Load rule from rule file and install handlers.
 */
public ParseRule createRule()
{
    ParseRule rule = super.createRule();           // Load KeyChain rules from KeyChainParser.txt
    ParseUtils.installHandlers(getClass(), rule);  // Install Handlers
    return rule.getRule("Statement");              // Return Statement rule
}

/**
 * Statement Handler.
 */
public static class StatementHandler extends ParseHandler <RMKeyChain> {

    // The Op
    int    _op;

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle KEY
        if(anId=="KEY")
            _part = new RMKeyChain(Op.Key, aNode.getString());
        
        // Handle Expression
        else if(anId=="Expression") {
            RMKeyChain expr = aNode.getCustomNode(RMKeyChain.class);
            if(_part==null) _part = expr;
            else if(_op==1) _part = new RMKeyChain(Op.Assignment, _part, expr);
            else _part = new RMKeyChain(Op.Assignment, _part, new RMKeyChain(RMKeyChain.Op.Add, _part, expr));
            _op = 0;
        }
        
        // Handle Ops
        else if(anId=="=") _op = 1;
        else if(anId=="+=") _op = 2;
    }
}

/**
 * Expression Handler.
 */
public static class ExpressionHandler extends ParseHandler <RMKeyChain> {

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle KeyChain
        if(aNode.getCustomNode() instanceof RMKeyChain) { RMKeyChain kc = aNode.getCustomNode(RMKeyChain.class);
            if(_part==null) _part = kc;
            else if(_part.getOp()!=Op.Conditional) _part = new RMKeyChain(Op.Conditional, _part, kc);
            else _part.addChild(kc);
        }
    }
}

/**
 * LogicalOrExpr Handler.
 */
public static class LogicalOrExprHandler extends BinaryExprHandler { }

/**
 * LogicalAndExpr Handler.
 */
public static class LogicalAndExprHandler extends BinaryExprHandler { }

/**
 * EqualityExpr Handler.
 */
public static class EqualityExprHandler extends BinaryExprHandler { }

/**
 * ComparativeExpr Handler.
 */
public static class ComparativeExprHandler extends BinaryExprHandler { }

/**
 * AdditiveExpr Handler.
 */
public static class AdditiveExprHandler extends BinaryExprHandler { }

/**
 * MultiplicativeExpr Handler.
 */
public static class MultiplicativeExprHandler extends BinaryExprHandler { }

/**
 * OpExpr Handler.
 */
public static abstract class BinaryExprHandler extends ParseHandler <RMKeyChain> {

    // The Op
    Op _op; RMKeyChain  _more;

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle KeyChain
        if(aNode.getCustomNode() instanceof RMKeyChain) {
            RMKeyChain kc = aNode.getCustomNode(RMKeyChain.class);
            if(_part==null) { _part = kc; _more = null; }
            else if(_more==null) _part = _more = new RMKeyChain(_op, _part, kc);
            else _more.setChild(_more = new RMKeyChain(_op, _more.getChild(1), kc), 1);
        }
        
        // Handle Ops
        else if(anId=="+") _op = Op.Add;
        else if(anId=="-") _op = Op.Subtract;
        else if(anId=="*") _op = Op.Multiply;
        else if(anId=="/") _op = Op.Divide;
        else if(anId=="%") _op = Op.Mod;
        else if(anId=="==") _op = Op.Equal;
        else if(anId=="!=") _op = Op.NotEqual;
        else if(anId==">") _op = Op.GreaterThan;
        else if(anId=="<") _op = Op.LessThan;
        else if(anId==">=") _op = Op.GreaterThanOrEqual;
        else if(anId=="<=") _op = Op.LessThanOrEqual;
        else if(anId=="||") _op = Op.Or;
        else if(anId=="&&") _op = Op.And;
    }
}

/**
 * UnaryExpr Handler.
 */
public static class UnaryExprHandler extends ParseHandler <RMKeyChain> {

    // The Op
    Op _op;

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle KeyChain
        if(anId=="KeyChain") {
            RMKeyChain kc = aNode.getCustomNode(RMKeyChain.class);
            _part = _op==null? kc : new RMKeyChain(_op, kc); _op = null;
        }
        
        // Handle Ops
        else if(anId=="-") _op = Op.Subtract;
        else if(anId=="!") _op = Op.Not;
    }
}

/**
 * KeyChain Handler.
 */
public static class KeyChainHandler extends ParseHandler <RMKeyChain> {

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Object
        if(anId=="Object") {
            RMKeyChain kc = aNode.getCustomNode(RMKeyChain.class);
            if(_part==null) _part = kc;
            else if(_part.getOp()!=Op.Chain) _part = new RMKeyChain(Op.Chain, _part, kc);
            else _part.addChild(kc);
        }
    }
}

/**
 * Object Handler.
 */
public static class ObjectHandler extends ParseHandler <RMKeyChain> {

    // Whether starting args
    boolean   _startArgs;

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Key
        if(anId=="KEY")
            _part = new RMKeyChain(Op.Key, aNode.getString());
        
        // Handle ArgList
        else if(anId=="ArgList") {
            RMKeyChain args = aNode.getCustomNode(RMKeyChain.class);
            _part = new RMKeyChain(Op.FunctionCall, _part.getChildString(0), args);
        }
        
        // Handle empty ArgList
        else if(anId=="(") { _startArgs = _part!=null; }
        else if(anId==")") {
            if(_startArgs && _part.getOp()==Op.Key) {
                _part = new RMKeyChain(Op.FunctionCall, _part.getChildString(0), new RMKeyChain(Op.ArgList));
                _startArgs = false; }
        }
        
        // Handle INT or Float
        else if(anId=="INT" || anId=="FLOAT") {
            java.math.BigDecimal d = new java.math.BigDecimal(aNode.getString());
            _part = new RMKeyChain(Op.Literal, d);
        }
        
        // Handle STRING
        else if(anId=="STRING") {
            String str = aNode.getString(); str = str.substring(1, str.length()-1); // Strip quotes
            _part = new RMKeyChain(Op.Literal, str);
        }

        // Handle Expression
        else if(anId=="Expression") {
            RMKeyChain expr = aNode.getCustomNode(RMKeyChain.class);
            _part = _part!=null? new RMKeyChain(Op.ArrayIndex, _part, expr) : expr;
        }
    }
}

/**
 * ArgList Handler.
 */
public static class ArgListHandler extends ParseHandler <RMKeyChain> {

    /** ParseHandler method. */
    public void parsedOne(ParseNode aNode, String anId)
    {
        // Handle Expression
        if(anId=="Expression") {
            RMKeyChain arg = aNode.getCustomNode(RMKeyChain.class);
            if(_part==null) _part = new RMKeyChain(Op.ArgList, arg);
            else _part.addChild(arg);
        }
    }
}

}