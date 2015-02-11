/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snap.web;
import com.reportmill.base.RMKeyChain;

/**
 * A class to parse a condition string and return a condition.
 */
public class ConditionParser {

/**
 * Parse a condition.
 */
public Condition parse(String aString)
{
    // Get key chain and convert to condition
    RMKeyChain keyChain = RMKeyChain.getKeyChain(aString);
    return getKeyChainAsCondition(keyChain);
}

/**
 * Converts a key chain to a condition.
 */
protected Condition getKeyChainAsCondition(RMKeyChain aKeyChain)
{
    // Handle op
    RMKeyChain.Op keyChainOp = aKeyChain.getOp();
    switch(keyChainOp) {
    
        // Handle comparison operators: Equal, LessThan, GreaterThan, NotEqual
        case Equal: case LessThan: case LessThanOrEqual: case GreaterThan: case GreaterThanOrEqual: case NotEqual:
            Condition condition = new Condition();
            condition.setPropertyName(aKeyChain.getChildString(0));
            condition.setOperator(getConditionOperator(keyChainOp));
            condition.setValue(aKeyChain.getChildString(1));
            return condition;
            
        // Handle boolean operators: And, Or
        case And: case Or:
            if(aKeyChain.getChild(0) instanceof RMKeyChain) {
                Condition part1 = getKeyChainAsCondition(aKeyChain.getChildKeyChain(0));
                Condition part2 = getKeyChainAsCondition(aKeyChain.getChildKeyChain(1));
                ConditionList conditionList = new ConditionList();
                conditionList.addCondition(Condition.Operator.And, part1);
                conditionList.addCondition(getConditionOperator(keyChainOp), part2);
                return conditionList;
            }
            
        // Complain about anything else
        default: throw new RuntimeException("Operator not supported: " + aKeyChain.getOp());
    }
}

/**
 * Converts a comparison key chain to a condition.
 */
protected Condition.Operator getConditionOperator(RMKeyChain.Op anOp)
{
    switch(anOp) {
        case Equal: return Condition.Operator.Equals;
        case LessThan: return Condition.Operator.LessThan;
        case LessThanOrEqual: return Condition.Operator.LessThanOrEqual;
        case GreaterThan: return Condition.Operator.GreaterThan;
        case GreaterThanOrEqual: return Condition.Operator.GreaterThanOrEqual;
        case And: return Condition.Operator.And;
        case Or: return Condition.Operator.Or;
        case NotEqual: return Condition.Operator.Equals;
        default: throw new RuntimeException("Operator not supported: " + anOp);
    }
}

}