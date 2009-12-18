 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.eclipse.codeassist.proposals;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 * 
 */
public class PropertyProposalCreator extends AbstractProposalCreator implements IProposalCreator {
    

    public List<IGroovyProposal> findAllProposals(ClassNode type,
            Set<ClassNode> categories, String prefix, boolean isStatic) {
        Collection<PropertyNode> allProperties = getAllProperties(type);
        List<IGroovyProposal> groovyProposals = new LinkedList<IGroovyProposal>();
        for (PropertyNode property : allProperties) {
            if ((!isStatic || property.isStatic()) &&
                    ProposalUtils.looselyMatches(prefix, property.getName())) {
                groovyProposals.add(new GroovyPropertyProposal(property));
            }
        }
        
        return groovyProposals;
    }
    
    private Collection<PropertyNode> getAllProperties(ClassNode thisType) {
        Set<ClassNode> types = new HashSet<ClassNode>();
        getAllSupers(thisType, types);
        List<PropertyNode> allProperties = new LinkedList<PropertyNode>();
        for (ClassNode type : types) {
            type = type.redirect();
            for (PropertyNode property : (Iterable<PropertyNode>) type.getProperties()) {
                if (checkName(property.getName())) {
                    allProperties.add(property);
                }
            }
        }
        return allProperties;
    }
}
