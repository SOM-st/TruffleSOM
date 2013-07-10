/**
 * Copyright (c) 2013 Stefan Marr, stefan.marr@vub.ac.be
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
package som.interpreter.nodes;

import som.compiler.MethodGenerationContext;
import som.vm.Universe;
import som.vmobjects.Block;
import som.vmobjects.Object;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;

public class ReturnNonLocalNode extends ContextualNode {

  @Child protected final ExpressionNode expression;
  private final Universe universe;

  public ReturnNonLocalNode(final ExpressionNode expression,
      final int contextLevel,
      final Universe universe) {
    super(contextLevel);
    this.expression = adoptChild(expression);
    this.universe   = universe;
  }

  @Override
  public Object executeGeneric(VirtualFrame frame) {
    try {
      MaterializedFrame ctx = determineContext(frame.materialize());
      FrameOnStackMarker marker = (FrameOnStackMarker) ctx.
          getObject(MethodGenerationContext.
              getStandardNonLocalReturnMarkerSlot());

      if (marker.isOnStack()) {
        throw new ReturnException(expression.executeGeneric(frame), marker);
      } else {
        FrameSlot selfSlot = MethodGenerationContext.getStandardSelfSlot();

        Block block = (Block) frame.getObject(selfSlot);
        Object self = (Object) ctx.getObject(selfSlot);

        return self.sendEscapedBlock(block, universe, frame.pack());
      }
    } catch (FrameSlotTypeException e) {
      throw new RuntimeException("This should never happen! really!");
    }
  }
}
