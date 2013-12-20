/**
 * Copyright (c) 2013 Stefan Marr,   stefan.marr@vub.ac.be
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
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

package som.vmobjects;

import som.interpreter.Arguments;
import som.interpreter.Invokable;
import som.interpreter.Types;
import som.vm.Universe;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleRuntime;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.PackedFrame;

public class SMethod extends SAbstractObject {

  public SMethod(final SSymbol signature, final Invokable truffleInvokable,
      final FrameDescriptor frameDescriptor, final boolean isPrimitive) {
    this.signature = signature;

    this.truffleInvokable = truffleInvokable;

    TruffleRuntime runtime =  Truffle.getRuntime(); // TODO: should be: universe.getTruffleRuntime();
    CallTarget target = runtime.createCallTarget(truffleInvokable, frameDescriptor);
    this.callTarget = target;
    this.isPrimitive = isPrimitive;
  }

  public CallTarget getCallTarget() {
    return callTarget;
  }

  public Invokable getTruffleInvokable() {
    return truffleInvokable;
  }

  public boolean isPrimitive() {
    return isPrimitive;
  }

  public SSymbol getSignature() {
    return signature;
  }

  public SClass getHolder() {
    return holder;
  }

  public void setHolder(final SClass value) {
    holder = value;
  }

  public int getNumberOfArguments() {
    // Get the number of arguments of this method
    return getSignature().getNumberOfSignatureArguments();
  }

  public SAbstractObject invokeRoot(final SAbstractObject self,
      final SAbstractObject[] args, final Universe universe) {
    SAbstractObject result = Types.asAbstractObject(callTarget.call(new Arguments(self,
        args, truffleInvokable.getNumberOfUpvalues(), universe.nilObject)), universe);
    return result;
  }

  public Object invoke(final PackedFrame caller, final Object self,
      final Universe universe) {
    return callTarget.call(caller, new Arguments(self, null,
        truffleInvokable.getNumberOfUpvalues(), universe.nilObject));
  }

  public Object invoke(final PackedFrame caller, final Object self,
      final Object arg, final Universe universe) {
    return callTarget.call(caller, new Arguments(self,
        new Object[] {arg}, truffleInvokable.getNumberOfUpvalues(), universe.nilObject));
  }

  public Object invoke(final PackedFrame caller, final Object self,
      final Object arg1, final Object arg2, final Universe universe) {
    return callTarget.call(caller, new Arguments(self,
        new Object[] {arg1, arg2}, truffleInvokable.getNumberOfUpvalues(), universe.nilObject));
  }

  public Object invoke(final PackedFrame caller, final Object self,
      final Object[] args, final Universe universe) {
    Object result = callTarget.call(caller, new Arguments(self, args,
        truffleInvokable.getNumberOfUpvalues(), universe.nilObject));
    return result;
  }

  @Override
  public SClass getSOMClass(final Universe universe) {
    if (isPrimitive) {
      return universe.primitiveClass;
    } else {
      return universe.methodClass;
    }
  }

  @Override
  public String toString() {
    // TODO: fixme: remove special case if possible, I think it indicates a bug
    if (holder == null) {
      return "Method(nil>>" + getSignature().toString() + ")";
    }

    return "Method(" + getHolder().getName().getString() + ">>" + getSignature().toString() + ")";
  }

  // Private variable holding Truffle runtime information
  private final Invokable              truffleInvokable;
  private final CallTarget             callTarget;
  private final boolean                isPrimitive;
  private final SSymbol                signature;
  @CompilationFinal private SClass     holder;
}
