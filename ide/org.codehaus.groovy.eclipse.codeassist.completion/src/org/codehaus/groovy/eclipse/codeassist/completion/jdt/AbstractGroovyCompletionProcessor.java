/*******************************************************************************
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
 *******************************************************************************/
package org.codehaus.groovy.eclipse.codeassist.completion.jdt;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.impl.SourceCodeContextFactory;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.GroovyDeclaration;
import org.codehaus.groovy.eclipse.core.types.LocalVariable;
import org.codehaus.groovy.eclipse.core.types.Member;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Modifiers;
import org.codehaus.groovy.eclipse.core.types.Parameter;
import org.codehaus.groovy.eclipse.core.types.Property;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ParseException;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("nls")
public abstract class AbstractGroovyCompletionProcessor implements IJavaCompletionProposalComputer {
	
	/**
	 * Remove types that are equal. There is no control over if the different implementations of IMemberLookup will
	 * equal result, so this method merges the duplicates if they exist.
	 * 
	 * @param types
	 * @return
	 */
    protected GroovyDeclaration[] mergeTypes(GroovyDeclaration[] types) {
		if (types.length < 1) {
			return types;
		}
		
		Arrays.sort(types);
		
		List<GroovyDeclaration> results = new ArrayList<GroovyDeclaration>();
		results.add(types[0]);
		for (int i = 1; i < types.length; ++i) {
			if (!isSimilar(types[i-1], types[i])) {
				results.add(types[i]);
			}
		}
		
		removeCompilerMethods(results);
		
		// HACK: emp - must be base type. This is really wanting to be an interface at some stage.
		Class<?> cls = types[0].getClass();
		if (cls.getName().startsWith("org.codehaus.groovy.eclipse.core.types.Java")) {
			cls = cls.getSuperclass();
		}
		
		return (GroovyDeclaration[]) results.toArray((GroovyDeclaration[])Array.newInstance(cls, results.size()));
	}

    private boolean isSimilar(GroovyDeclaration type1, GroovyDeclaration type2) {
        if (type1 instanceof Method && type2 instanceof Method) {
            Method method1 = (Method) type1;
            Method method2 = (Method) type2;
            
            if (! method1.getName().equals(method2.getName())) {
                return false;
            }
            
            if (method1.getParameters().length != method2.getParameters().length) {
                return false;
            }
            
            Parameter[] params1 = method1.getParameters();
            Parameter[] params2 = method2.getParameters();
            for (int i = 0; i < params1.length; i++) {
                if (!params1[i].getSignature() .equals(params2[i].getSignature())) {
                    return false;
                }
            }
            return true;
        } else {
            return type1.equals(type2);
        }
    }
	
	/**
	 * class$ and super$ and this$ methods must be removed.
	 * @param results
	 */
	protected void removeCompilerMethods(List<GroovyDeclaration> results) {
		for (Iterator<GroovyDeclaration> iter  = results.iterator(); iter.hasNext();) {
			String name = iter.next().getName();
			if (name.startsWith("<clinit>") || name.startsWith("class$") || name.startsWith("super$") || name.startsWith("this$")) {
				iter.remove();
			}
		}
	}
	
    protected boolean isScriptOrClosureContext(ISourceCodeContext sourceContext) {
        try {
            return  sourceContext.getId().equals(ISourceCodeContext.CLOSURE_SCOPE) ||
                    ((ClassNode) sourceContext.getASTPath()[1]).isScript();
        } catch (Exception e) {
            // any reason for failure means we are not in a script
            return false;
        }
    }


	protected Image getImageForType(GroovyDeclaration type) {
		if (type instanceof LocalVariable) {
			return JavaPluginImages.get(JavaPluginImages.IMG_OBJS_LOCAL_VARIABLE);
		}
		if (type instanceof Field) {
			if (test(type.getModifiers(), Modifiers.ACC_PUBLIC)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
			} else if (test(type.getModifiers(), Modifiers.ACC_PROTECTED)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PROTECTED);
			} else if (test(type.getModifiers(), Modifiers.ACC_PRIVATE)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PRIVATE);
			}
			return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_DEFAULT);
		} else if (type instanceof Property) {
			// TODO: need compound icon with 'r' and 'w' on it to indicate property and access.
			if (test(type.getModifiers(), Modifiers.ACC_PUBLIC)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PUBLIC);
			} else if (test(type.getModifiers(), Modifiers.ACC_PROTECTED)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PROTECTED);
			} else if (test(type.getModifiers(), Modifiers.ACC_PRIVATE)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_PRIVATE);
			}
			return JavaPluginImages.get(JavaPluginImages.IMG_FIELD_DEFAULT);
		} else if (type instanceof Method) {
			if (test(type.getModifiers(), Modifiers.ACC_PUBLIC)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PUBLIC);
			} else if (test(type.getModifiers(), Modifiers.ACC_PROTECTED)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PROTECTED);
			} else if (test(type.getModifiers(), Modifiers.ACC_PRIVATE)) {
				return JavaPluginImages.get(JavaPluginImages.IMG_MISC_PRIVATE);
			}
			return JavaPluginImages.get(JavaPluginImages.IMG_MISC_DEFAULT);
		}
		return null;
	}

	protected boolean test(int flags, int mask) {
		return (flags & mask) != 0;
	}

	protected String createReplaceString(GroovyDeclaration method) {
		return method.getName() + "()";
	}
	
	protected StyledString createDisplayString(Member member) {
		StringBuffer sb = new StringBuffer(member.getName());
		sb.append(" ").append(toSimpleTypeName(member.getSignature())).append(" - ").append(
				toSimpleTypeName(member.getDeclaringClass().getSignature()));
		return new StyledString(sb.toString());
	}

	protected char[] createDisplayString(Method method) {
		StringBuffer sb = new StringBuffer(method.getName() + "(");
		String returnType = method.getReturnType();
		if (returnType != null) {
			returnType = toSimpleTypeName(returnType);
		} else {
			returnType = "void";
		}
		sb.append(createParameterListString(method)).append(")");
		sb.append(" ").append(returnType);
		sb.append(" - ").append(toSimpleTypeName(method.getDeclaringClass().getSignature()));

		return sb.toString().toCharArray();
	}

	protected String createParameterListString(Method method) {
		StringBuffer sb = new StringBuffer();
		Parameter[] parameter = method.getParameters();
		if (parameter.length == 0) {
			return "";
		}
		for (int i = 0; i < parameter.length; ++i) {
			sb.append(toSimpleTypeName(parameter[i].getSignature())).append(" ").append(parameter[i].getName())
					.append(", ");
		}
		return sb.substring(0, sb.length() - 2);
	}

	protected String toSimpleTypeName( final String t ) {
	    String type = t;
		if (type.charAt(0) == '[') {
			type = Signature.toString(type);
		}
		int ix = type.lastIndexOf('.');
		if (ix != -1) {
			return type.substring(ix + 1);
		}
		return type;
	}

	
	protected String findCompletionExpression(ExpressionFinder finder, int offset, ISourceBuffer buffer) {
		try{
			return finder.findForCompletions(buffer, offset - 1);
		} catch (ParseException e) {
            // can ignore.  probably just invalid code that is being completed at
            GroovyCore.trace("Cannot complete code:" + e.getMessage());
		}
		return null;
	}

	protected ISourceCodeContext[] createContexts(ModuleNode moduleNode, ISourceBuffer buffer, int offset) {
		SourceCodeContextFactory factory = new SourceCodeContextFactory();
		return factory.createContexts(buffer, moduleNode, new Region(offset, 0));
	}

	protected ModuleNode getCurrentModuleNode(JavaContentAssistInvocationContext context) {
	    return ((GroovyCompilationUnit) context.getCompilationUnit()).getModuleNode();
	}

    public String getErrorMessage() {
        return null;
    }

    public void sessionEnded() {
        // do nothing
    }

    public void sessionStarted() {
        // do nothing
    }
    
    private static final List<IContextInformation> NO_CONTEXTS= Collections.emptyList();
    public List<IContextInformation> computeContextInformation(
            ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return NO_CONTEXTS;
    }

}
