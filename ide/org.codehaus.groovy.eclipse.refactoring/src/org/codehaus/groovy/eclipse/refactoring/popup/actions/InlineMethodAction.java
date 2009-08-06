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
package org.codehaus.groovy.eclipse.refactoring.popup.actions;

import org.codehaus.groovy.eclipse.refactoring.core.GroovyRefactoring;
import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodInfo;
import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodProvider;
import org.codehaus.groovy.eclipse.refactoring.core.inlineMethod.InlineMethodRefactoring;
import org.eclipse.jface.action.IAction;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

/**
 * @author mike klenk
 * 
 */
public class InlineMethodAction extends GroovyRefactoringAction {

	public void run(IAction action) {
		if (initRefactoring()) {
			InlineMethodProvider provider = new InlineMethodProvider(docProvider, selection);
			InlineMethodInfo info = new InlineMethodInfo(provider);

			GroovyRefactoring groovyRefactoring = new InlineMethodRefactoring(info);
			openRefactoringWizard(groovyRefactoring);
		}
	}

	@Override
    protected int getUIFlags() {
		return RefactoringWizard.DIALOG_BASED_USER_INTERFACE
				| RefactoringWizard.PREVIEW_EXPAND_FIRST_NODE;
	}
}
