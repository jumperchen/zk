/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* Generated By:JJTree: Do not edit this line. SimpleNode.java */

package org.zkoss.zel.impl.parser;

import java.util.Arrays;

import org.zkoss.zel.ELException;
import org.zkoss.zel.MethodInfo;
import org.zkoss.zel.PropertyNotWritableException;
import org.zkoss.zel.ValueReference;
import org.zkoss.zel.impl.lang.ELSupport;
import org.zkoss.zel.impl.lang.EvaluationContext;
import org.zkoss.zel.impl.util.MessageFactory;


/**
 * @author Jacob Hookom [jacob@hookom.net]
 */
public abstract class SimpleNode extends ELSupport implements Node {
    protected Node parent;

    protected Node[] children;

    protected final int id;

    protected String image;

    public SimpleNode(int i) {
        id = i;
    }

    
    public void jjtOpen() {
        // NOOP by default
    }

    
    public void jjtClose() {
        // NOOP by default
    }

    
    public void jjtSetParent(Node n) {
        parent = n;
    }

    
    public Node jjtGetParent() {
        return parent;
    }

    
    public void jjtAddChild(Node n, int i) {
        if (children == null) {
            children = new Node[i + 1];
        } else if (i >= children.length) {
            Node c[] = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }

    
    public Node jjtGetChild(int i) {
        return children[i];
    }

    
    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.length;
    }

    /*
     * You can override these two methods in subclasses of SimpleNode to
     * customize the way the node appears when the tree is dumped. If your
     * output uses more than one line you should override toString(String),
     * otherwise overriding toString() is probably all you need to do.
     */

    
    public String toString() {
        if (this.image != null) {
            return ELParserTreeConstants.jjtNodeName[id] + "[" + this.image
                    + "]";
        }
        return ELParserTreeConstants.jjtNodeName[id];
    }

    
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    
    public Class<?> getType(EvaluationContext ctx)
            throws ELException {
        throw new UnsupportedOperationException();
    }

    
    public Object getValue(EvaluationContext ctx)
            throws ELException {
        throw new UnsupportedOperationException();
    }

    
    public boolean isReadOnly(EvaluationContext ctx)
            throws ELException {
        return true;
    }

    
    public void setValue(EvaluationContext ctx, Object value)
            throws ELException {
        throw new PropertyNotWritableException(MessageFactory.get("error.syntax.set"));
    }

    
    public void accept(NodeVisitor visitor) throws Exception {
        visitor.visit(this);
        if (this.children != null && this.children.length > 0) {
            for (int i = 0; i < this.children.length; i++) {
                this.children[i].accept(visitor);
            }
        }
    }

    
    public Object invoke(EvaluationContext ctx, Class<?>[] paramTypes,
            Object[] paramValues) throws ELException {
        throw new UnsupportedOperationException();
    }

    
    public MethodInfo getMethodInfo(EvaluationContext ctx,
            Class<?>[] paramTypes) throws ELException {
        throw new UnsupportedOperationException();
    }


    
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(children);
        result = prime * result + id;
        result = prime * result + ((image == null) ? 0 : image.hashCode());
        return result;
    }

    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SimpleNode)) {
            return false;
        }
        SimpleNode other = (SimpleNode) obj;
        if (!Arrays.equals(children, other.children)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (image == null) {
            if (other.image != null) {
                return false;
            }
        } else if (!image.equals(other.image)) {
            return false;
        }
        return true;
    }

    /**
     * @since EL 2.2
     */
    
    public ValueReference getValueReference(EvaluationContext ctx) {
        return null;
    }

    /**
     * @since EL 2.2
     */
    
    public boolean isParametersProvided() {
        return false;
    }
    
  //20120331, henrichen@zkoss.org
    /**
     * Returns total siblings (including self) of this node; so at least return one.
     * @since ZEL 2.2.2
     */
    public int jjtGetNumSiblings() {
    	final Node parent = jjtGetParent();
    	return parent == null ? 1 : parent.jjtGetNumChildren();
    }
}
