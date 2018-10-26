package test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.ConcreteJavaMethod;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.thin.ThinSlicer;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;

public class tt {
	Collection<Statement> collection = null;

	public static int returnNum(String in) {
		if (in.equals("a"))
			return 0;
		return 1;
	}

	public static void print(CallGraph cg) {
		int i = 1;
		for (Iterator<? extends CGNode> it = cg.iterator(); it.hasNext();) {

			CGNode n = it.next(); // �������������Ƚ�
			System.out.println("method:  " + n.getMethod().getName() + "    class:  "
					+ n.getMethod().getDeclaringClass().getName().toString());
			System.err.println(n);

		}

	}

	public static void printSDG(SDG<?> sdg) {
		int i = 0;
		for (Iterator<Statement> it = sdg.iterator(); it.hasNext();) {
			i = 0;
			Statement state = it.next(); // �������������Ƚ�
			if (state.getKind() == Statement.Kind.NORMAL) { // ignore special kinds of statements
				int bcIndex, instructionIndex = ((NormalStatement) state).getInstructionIndex();
				try {
					bcIndex = ((ShrikeBTMethod) state.getNode().getMethod()).getBytecodeIndex(instructionIndex);
					try {
						i = state.getNode().getMethod().getLineNumber(bcIndex);

					} catch (Exception e) {
						System.err.println("Bytecode index no good");
						System.err.println(e.getMessage());
					}
				} catch (Exception e) {
					System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
					i = 1;
					System.err.println(e.getMessage());
				}
			}

			System.out.println("statement:  " + state.toString() + "----------" + i);
			// System.err.println(n);

		}

	}

	// ������Ҫ֪����seed���ĸ������У����ԣ�����Ҫ����cgͼ���ҵ��ĸ��ڵ��Ƕ�Ӧ�ĺ�����
	// cgͼ�����������������ڵ���
	public static CGNode findMethod(CallGraph cg, String Name, String methodCLass) {
		if (Name.equals(null) && methodCLass.equals(null))
			return null; // ���� atom��wala�У��������Ƚ�CGNode�����ֺ���
		Atom name = Atom.findOrCreateUnicodeAtom(Name); // ����cgͼ
		int i = 1;
		for (Iterator<? extends CGNode> it = cg.iterator(); it.hasNext();) {

			CGNode n = it.next(); // �������������Ƚ�
			// System.out.println(n.getMethod().getName()+"sss"+n.getMethod().getDeclaringClass().getName().toString());
			// System.out.println(name+"dddd"+methodCLass);
			if (n.getMethod().getName().equals(name)
					&& n.getMethod().getDeclaringClass().getName().toString().equals(methodCLass)) {

				return n;
			}
		}
		Assertions.UNREACHABLE("Failed to find method " + name);
		return null;
	}

	// ��CGNode���ҵ�seed
	// n�ǵڶ����ķ���ֵ��methodName��seed�ĺ�����
	public static Statement findCallTo(CGNode n, String methodName) {
		// ����n��ir���м��ʾ����llvm��ir�����ƣ���
		IR ir = n.getIR();
		// System.out.println(ir.toString());
		// ����ir��ÿһ��ָ�Ѱ��seed
		for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
			SSAInstruction s = it.next(); // ��ǰָ���ǵ���ָ��ȽϺ��������Ƿ���seed��
			if (s instanceof SSAInvokeInstruction) {
				SSAInvokeInstruction call = (SSAInvokeInstruction) s;
				if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {
					IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
					Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
					return new NormalStatement(n, indices.intIterator().next());
				}
			}
		}
		Assertions.UNREACHABLE("Failed to find call to " + methodName + " in " + n);
		return null;
	}

	public static Statement findStatement(SDG<?> sdg, String methodName) {
		// ����n��ir���м��ʾ����llvm��ir�����ƣ���
		// IR ir = n.getIR();
		// System.out.println(ir.toString());
		// ����ir��ÿһ��ָ�Ѱ��seed
		int i = 0;
		for (Iterator<Statement> it = sdg.iterator(); it.hasNext();) {
			i = 0;
			Statement state = it.next(); // �������������Ƚ�
			if (state.getKind() == Statement.Kind.NORMAL) { // ignore special kinds of statements
				try {

					state.getNode().getMethod();

				} catch (Exception e) {
					System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
					i = 1;
					System.err.println(e.getMessage());
				}
			}

			System.out.println("statement:  " + state.toString() + "----------" + i);
			// System.err.println(n);

		}

		return null;
	}

	// ��ӡ��Ƭ��Ϣ
	public static void printState(Statement s) {
		// ֱ�������statement
		//System.err.println("cgnode:" + s);
		if (s.getKind() == Statement.Kind.NORMAL
				&& !(s.getNode().getMethod().getName().equals(Atom.findOrCreateAsciiAtom("fakeRootMethod")))) {
			CGNode node = s.getNode();

			// ��Ӧ��method��
			IMethod method = node.getMethod();

			// ��Ӧ��class��
			IClass klass = s.getNode().getMethod().getDeclaringClass();

			// �����ͷ���
			System.out.println("method:" + method.getName() + ", class:" + klass.getName());

			// �����
			if (s.getKind() == Statement.Kind.NORMAL) { // ignore special kinds of statements
				int bcIndex, instructionIndex = ((NormalStatement) s).getInstructionIndex();
				try {
					bcIndex = ((ShrikeBTMethod) s.getNode().getMethod()).getBytecodeIndex(instructionIndex);
					try {
						int src_line_number = s.getNode().getMethod().getLineNumber(bcIndex);
						System.out.println("Source line number = " + src_line_number);
					} catch (Exception e) {
						System.err.println("Bytecode index no good");
						System.err.println(e.getMessage());
					}
				} catch (Exception e) {
					System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
					System.err.println(e.getMessage());
				}
			}
		}
		// ƥ�����һ����
//		if (s.getKind() == Statement.Kind.NORMAL) {
//		  int instructionIndex = ((NormalStatement) s).getInstructionIndex();
//		  int lineNum = ((ConcreteJavaMethod) s.getNode().getMethod()).getLineNumber(instructionIndex);
//		  System.out.println("Source line number = " + lineNum );
//		}
//
	}

	// ͨ������ͼ�ڵ��ȡĳ������������
	public static int getMethodLine(CGNode n) {
		// ����n��ir���м��ʾ����llvm��ir�����ƣ���
		IR ir = n.getIR();
		IBytecodeMethod<?> method = (IBytecodeMethod<?>) ir.getMethod();
		int bytecodeIndex = 0;
		try {
			bytecodeIndex = method.getBytecodeIndex(1);
		} catch (InvalidClassFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int sourceLineNum = method.getLineNumber(bytecodeIndex);
		return sourceLineNum;
	}

	public static Iterable<Entrypoint> makeMainEntrypoints(AnalysisScope scope, IClassHierarchy cha) {
		if (scope == null) {
			throw new IllegalArgumentException("scope is null");
		}
		return makeMainEntrypoints(scope.getApplicationLoader(), cha);
	}

	public static Iterable<Entrypoint> makeMainEntrypoints(ClassLoaderReference clr, IClassHierarchy cha) {
		if (cha == null) {
			throw new IllegalArgumentException("cha is null");
		}
		final Atom mainMethod = Atom.findOrCreateAsciiAtom("contains");
		System.out.println(mainMethod + "============");
		final HashSet<Entrypoint> result = HashSetFactory.make();
		for (IClass klass : cha) {
			if (klass.getClassLoader().getReference().equals(clr)) {
				MethodReference mainRef = MethodReference.findOrCreate(klass.getReference(), mainMethod,
						Descriptor.findOrCreateUTF8("(Ljava/lang/Object;)Z"));
				System.out.println(mainRef + "============");
				IMethod m = klass.getMethod(mainRef.getSelector());
				// System.out.println(m.getName());
				if (m != null) {
					result.add(new DefaultEntrypoint(m, cha));
				}
			}
		}
		return result::iterator;
	}

	public static void main(String args[]) throws IOException, ClassHierarchyException, IllegalArgumentException,
			InvalidClassFileException, CancelException {
		// ���һ���ļ�
		File exFile = new FileProvider().getFile("Java60RegressionExclusions.txt");
		// ��������浽�ļ���
		AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope("D:\\work\\Java\\mix\\list", exFile);
		// ����ClassHierarchy���൱�����һ���㼶�ṹ
		ClassHierarchy cha = ClassHierarchyFactory.make(scope); // ѭ������ÿһ����

		// ������ڵ�
		// Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope,
		// cha);//Util������main����Ϊ��ڵ�
		Iterable<Entrypoint> entrypoints = makeMainEntrypoints(scope, cha);// �ڱ������Լ��������ĳ������Ϊ��ڵ�
		// System.out.println(scope.getApplicationLoader().getName());
//		for (Entrypoint E : options.getEntrypoints()) {
//		System.out.println("Entrypoint: " + E);
//	}

		AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
		options.setReflectionOptions(ReflectionOptions.NONE);
		// build call graph
		CallGraphBuilder<InstanceKey> builder = Util.makeVanillaZeroOneCFABuilder(Language.JAVA, options,
				new AnalysisCacheImpl(), cha, scope);
		CallGraph cg = builder.makeCallGraph(options, null);
		// Build sdg
		ModRef<InstanceKey> modRef = ModRef.make();
		final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
		SDG<?> sdg = new SDG<>(cg, pa, modRef, DataDependenceOptions.FULL, ControlDependenceOptions.FULL, null);

		// CGNode n =cg.getNode(8);

		// ��ӡCG��SDG
		// print(cg);
		// System.out.println("-------------------------");
		 printSDG(sdg);

		// System.out.println(getMethodLine(findMethod(cg, "getMin", "Lpaixu/w")));

		// find the seed statement. The seed statement in this example is the first
		// instance of calling a indexOf() function in the contains function.
		//CGNode n = findMethod(cg, "contains", "Llist/LinkedList");
		//Statement statement = findCallTo(n, "indexOf");
		// �Զ���������ɣ�slicer���кܶ෽�����練����Ƭ�����������е�\

		// System.err.println("Statement: " + statement);

		// ��ͨslicer
		 //Collection<Statement> slice = Slicer.computeForwardSlice(sdg, statement);
		// ���Ҫʹ��thin Slicer
		//ThinSlicer ts = new ThinSlicer(cg, pa);
		//Collection<Statement> slice = ts.computeBackwardThinSlice(statement);

		// ������Ƭ������statement
//		for (Statement s : slice) {
//			if (s.getKind() == Statement.Kind.NORMAL) {
//				//System.out.println(i++);
//				// �����Ϣ
//				printState(s);
//			}
//		}

		// ѭ����������ͷ�����
		// ���ʵ����
		/*
		 * out: Lpaixu/w <init> bubbleSort getMiddle quickSort quick <init>
		 * registerNatives getClass hashCode equals clone toString notify notifyAll wait
		 * wait wait finalize <clinit>
		 */
//		for (IClass klass : cha) { // ��ӡ����
//			 //
//			// System.out.println("classssssssss");
//			// �жϵ�ǰ���Ƿ���zookeeper��
//			if (scope.isApplicationLoader(klass.getClassLoader())) {
//				// // ����zookeeper�е����ÿ����������������ӡ������
//				System.out.println(klass.getName().toString());
//				for (IMethod m : klass.getAllMethods()) {
//					 System.out.println(m.getName().toString());
//				}
//			} else {
//				//System.out.println("ssss");
//			}
//		}

	}
}
