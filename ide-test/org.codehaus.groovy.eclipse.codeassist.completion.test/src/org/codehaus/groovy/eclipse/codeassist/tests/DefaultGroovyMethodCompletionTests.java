/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codeassist.tests;

import org.codehaus.groovy.eclipse.codeassist.completion.jdt.GeneralGroovyCompletionProcessor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * @author Andrew Eisenberg
 * @created Jun 5, 2009
 * 
 * Tests DefaultGroovyMethods that they appear when and where expected
 *
 */
public class DefaultGroovyMethodCompletionTests extends CompletionTestCase {

    private static final String CONTENTS = "class Class { public Class() {\n }\n void doNothing(int x) { this.toString(); new Object().toString(); } }";
    private static final String SCRIPTCONTENTS = "def x = 9\nx++\nnew Object().toString()";
    private static final String CLOSURECONTENTS = "def x = { t -> print t }";

    public DefaultGroovyMethodCompletionTests(String name) {
        super(name);
    }
    
    // should not find dgm here
    public void testDGMInJavaFile() throws Exception {
        ICompilationUnit unit = createJava();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "this."), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "identity", 0);
    }

    // should find dgm here
    public void testDGMInMethodScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "this."), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "identity", 1);
    }

    // should find dgm here
    public void testDGMInMethodScopeFromOther() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "new Object()."), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "identity", 1);
    }
    
    // should find dgm here
    public void testDGMInConstructorScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "Class() {\n"), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "identity", 1);
    }

    // should find dgm here
    public void testDGMInScriptScope() throws Exception {
        ICompilationUnit unit = createGroovyForScript();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "\n"), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "identity", 1);
    }

    // should find dgm here
    public void testDGMInScriptOtherClassScope() throws Exception {
        ICompilationUnit unit = createGroovyForScript();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(SCRIPTCONTENTS, "new Object()."), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "identity", 1);
    }

    // should not find dgm here
    public void testDGMInClassScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "Class() { }"), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "identity", 0);
    }
    // should not find dgm here
    public void testDGMInMethodParamScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "void doNothing("), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "identity", 0);
    }
    // should not find dgm here
    public void testDGMInConstructorParamScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "Class("), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "identity", 0);
    }
    // should not find dgm here
    public void testDGMInModuleScope() throws Exception {
        ICompilationUnit unit = createGroovy();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CONTENTS, "; } }"), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "identity", 0);
    }
    // should find dgm here
    public void testDGMInClosure() throws Exception {
        ICompilationUnit unit = createGroovyForClosure();
        ICompletionProposal[] proposals = performContentAssist(unit, getIndexOf(CLOSURECONTENTS, " t -> "), GeneralGroovyCompletionProcessor.class);
        proposalExists(proposals, "identity", 1);
    }

    
    private ICompilationUnit createJava() throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addClass(src, "Class", CONTENTS);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
    
    private ICompilationUnit createGroovy() throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, "Class", CONTENTS);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
    private ICompilationUnit createGroovyForScript() throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, "Script", SCRIPTCONTENTS);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }
    private ICompilationUnit createGroovyForClosure() throws Exception {
        IPath projectPath = createGenericProject();
        IPath src = projectPath.append("src");
        IPath pathToJavaClass = env.addGroovyClass(src, "Closure", CLOSURECONTENTS);
        incrementalBuild();
        ICompilationUnit unit = getCompilationUnit(pathToJavaClass);
        return unit;
    }

    

}
