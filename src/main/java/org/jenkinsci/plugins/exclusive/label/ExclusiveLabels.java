/*
 * The MIT License
 *
 * Copyright 2012 lucinka.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.exclusive.label;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Queue.BuildableItem;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueTaskDispatcher;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

/**
 *
 * @author lucinka
 */
@Extension
public class ExclusiveLabels extends QueueTaskDispatcher implements Describable<ExclusiveLabels>{
    
    
    public List<LabelAtom> getExclusiveLabels(){
        return ((DescriptorImpl)getDescriptor()).getLabels();
    }
    
    public CauseOfBlockage canTake(Node node, BuildableItem item) {
        Label assignedLabel = item.task.getAssignedLabel();
        if(!assignedLabel.contains(node))
            return null;
        boolean containsExclusive = false;
        final List<LabelAtom> exclusives = new ArrayList<LabelAtom>();
        for(LabelAtom atom : node.getAssignedLabels()){
            if(getExclusiveLabels().contains(atom)){
               exclusives.add(atom);
               if(!containsExclusive)
                   containsExclusive=assignedLabel.getName().contains(atom.getName());
            }           
        }
        if(exclusives.size()<1)
            return null;
        if(!containsExclusive)
            return new NotExclusiveLabel(node);
        ExclusiveLabelVisitor visitor = new ExclusiveLabelVisitor();
        Set<LabelAtom> atoms = new HashSet<LabelAtom>();
        atoms.addAll(exclusives);
       
        if(!item.task.getAssignedLabel().accept(visitor, atoms))
            return new NotExclusiveLabel(node);
        return null;
    }

    public Descriptor<ExclusiveLabels> getDescriptor() {
        return Jenkins.getInstance().getDescriptorOrDie(this.getClass());
    }
    
    public class NotExclusiveLabel extends CauseOfBlockage{
        private Node node;
        
        public NotExclusiveLabel(Node node){
            this.node = node;
        }

        @Override
        public String getShortDescription() {
            return ("Node " + node.getDisplayName() + " has exclusive label(s)");
        }
        
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ExclusiveLabels>{
        
        private transient List<LabelAtom> exclusiveLabels = new ArrayList<LabelAtom>();
        private String labelsInString;
        
        public DescriptorImpl(){
            load();       
            exclusiveLabels = parseLabels(labelsInString);
        }
        
        public List<LabelAtom> getLabels(){
            return exclusiveLabels;
        }
        
        public static List<LabelAtom> parseLabels(String token){
            List<LabelAtom> atomLabels = new ArrayList<LabelAtom>();
            if(token==null || token.isEmpty())
                return atomLabels;
            String []labels = token.split(" ");
            for(String label:labels){
                atomLabels.add(new LabelAtom(label));
            }
            return atomLabels;
        }
        
        public String getLabelsInString(){
            return labelsInString;
        }

        @Override
        public String getDisplayName() {
            return "Exclusive labels";
        }
        
        @Override
        public boolean configure(StaplerRequest req, JSONObject res){
            labelsInString = req.getParameter("exclusiveLabels");
            exclusiveLabels = parseLabels(labelsInString);
            save();
            return true;
            
        }
        
    }
    
}
