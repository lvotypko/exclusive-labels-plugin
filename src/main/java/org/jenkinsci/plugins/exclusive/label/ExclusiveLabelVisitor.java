/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.exclusive.label;

import groovy.util.Node;
import hudson.model.labels.LabelAtom;
import hudson.model.labels.LabelExpression.And;
import hudson.model.labels.LabelExpression.Iff;
import hudson.model.labels.LabelExpression.Implies;
import hudson.model.labels.LabelExpression.Not;
import hudson.model.labels.LabelExpression.Or;
import hudson.model.labels.LabelExpression.Paren;
import hudson.model.labels.LabelVisitor;
import java.util.Set;

/**
 *
 * @author lucinka
 */
public class ExclusiveLabelVisitor extends LabelVisitor<Boolean,Set<LabelAtom>>{

    @Override
    public Boolean onAtom(LabelAtom a, Set<LabelAtom> param) {
       return param.contains(a);
    }

    @Override
    public Boolean onParen(Paren p, Set<LabelAtom> param) {
        return p.base.accept(this,param);
    }

    @Override
    public Boolean onNot(Not p, Set<LabelAtom> param) {
        if(p.base instanceof LabelAtom){
            return !(param.contains((LabelAtom)p.base));
        }
        return !(p.base.accept(this, param));
    }

    @Override
    public Boolean onAnd(And p, Set<LabelAtom> param) {
        boolean left = false;
        boolean right = false;
        if(p.lhs instanceof LabelAtom){
            left = param.contains((LabelAtom)p.lhs);
        }
        else{
            left = p.lhs.accept(this, param);
        }
        if(p.rhs instanceof LabelAtom){
            right = param.contains((LabelAtom)p.rhs);
        }
        else{
            right =  p.rhs.accept(this, param);
        }
        return left || right;
    }

    @Override
    public Boolean onOr(Or p, Set<LabelAtom> param) {
        boolean left = false;
        boolean right = false;
        if(p.lhs instanceof LabelAtom){
            left = param.contains((LabelAtom)p.lhs);
        }
        else{
            left = p.lhs.accept(this, param);
        }
        if(p.rhs instanceof LabelAtom){
            right = param.contains((LabelAtom)p.rhs);
        }
        else{
            right =  p.rhs.accept(this, param);
        }
        return left || right;
        
    }

    @Override
    public Boolean onIff(Iff p, Set<LabelAtom> param) {
        boolean left = false;
        boolean right = false;
        if(p.lhs instanceof LabelAtom){
            left = param.contains((LabelAtom)p.lhs);
        }
        else{
            left = p.lhs.accept(this, param);
        }
        if(p.rhs instanceof LabelAtom){
            right = param.contains((LabelAtom)p.rhs);
        }
        else{
            right =  p.rhs.accept(this, param);
        }
        return left || right;
    }

    @Override
    public Boolean onImplies(Implies p, Set<LabelAtom> param) {
        boolean right = true;
        if(p.rhs instanceof LabelAtom){
            right = param.contains((LabelAtom)p.rhs);
        }
        else{
            right = p.rhs.accept(this, param);
        }
        return right;
    }
    
}
