/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
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
package org.codehaus.groovy.eclipse.refactoring.core.extractMethod;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.codehaus.groovy.eclipse.refactoring.ui.pages.extractMethod.ExtractMethodPage;

/**
 * @author Michael Klenk mklenk@hsr.ch
 * Extract the current selection in a separate method and call this instead.
 *
 */
public class ExtractMethodRefactoring extends GroovyRefactoring {

	public ExtractMethodRefactoring(ExtractMethodInfo info) {
		super(info);
		setName(GroovyRefactoringMessages.ExtractMethodRefactoring);
		pages.add(new ExtractMethodPage(GroovyRefactoringMessages.ExtractMethodRefactoring,info));
	}
}
