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
package org.codehaus.groovy.eclipse.core.types.impl;


import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.AbstractASTBasedMemberLookup;

/**
 * Looks up members in Groovy classes throughout the current project.
 * 
 * @author empovazan
 */
public class GroovyProjectMemberLookup extends AbstractASTBasedMemberLookup {
  protected GroovyProjectFacade model;

	public GroovyProjectMemberLookup(GroovyProjectFacade model) {
		this.model = model;
	}
	
	protected ClassNode getClassNodeForName(String type) {
	    return model.getClassNodeForName(type);
	}
}