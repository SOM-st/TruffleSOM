package trufflesom.compiler;

import com.oracle.truffle.api.source.Source;

import trufflesom.bdt.basic.ProgramDefinitionError;
import trufflesom.bdt.tools.structure.StructuralProbe;
import trufflesom.interpreter.operations.SomOperationsGen.Builder;
import trufflesom.vmobjects.SClass;
import trufflesom.vmobjects.SInvokable;
import trufflesom.vmobjects.SSymbol;


public class ParserOp extends ParserAst {

  private Builder builder;

  public ParserOp(final String code, final Source source,
      final StructuralProbe<SSymbol, SClass, SInvokable, Field, Variable> probe) {
    super(code, source, probe);
  }

  @Override
  public void classdef(final ClassGenerationContext cgenc) throws ProgramDefinitionError {
    super.classdef(cgenc);
    cgenc.convertMethods(builder);
  }

  public void setBuilder(final Builder builder) {
    this.builder = builder;
  }
}