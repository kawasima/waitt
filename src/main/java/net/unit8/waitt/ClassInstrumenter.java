package net.unit8.waitt;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.instrument.FirstPassMethodInstrumenter;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Collection;

/**
 * @author kawasima
 */
@SuppressWarnings("rawtypes")
class ClassInstrumenter extends ClassAdapter
{

    private static final Logger logger = Logger
            .getLogger(ClassInstrumenter.class);

    private final static String hasBeenInstrumented = "net/sourceforge/cobertura/coveragedata/HasBeenInstrumented";

    private Collection ignoreRegexs;

    private Collection ignoreBranchesRegexs;

    private ProjectData projectData;

    private ClassData classData;

    private String myName;

    private boolean instrument = false;

    public String getClassName()
    {
        return this.myName;
    }

    public boolean isInstrumented()
    {
        return instrument;
    }

    public ClassInstrumenter(ProjectData projectData, final ClassVisitor cv,
                             final Collection ignoreRegexs, final Collection ignoreBranchesRegexs)
    {
        super(cv);
        this.projectData = projectData;
        this.ignoreRegexs = ignoreRegexs;
        this.ignoreBranchesRegexs = ignoreBranchesRegexs;
    }

    private boolean arrayContains(Object[] array, Object key)
    {
        for (int i = 0; i < array.length; i++)
        {
            if (array[i].equals(key))
                return true;
        }

        return false;
    }

    /**
     * @param name In the format
     *             "net/sourceforge/cobertura/coverage/ClassInstrumenter"
     */
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces)
    {
        this.myName = name.replace('/', '.');
        this.classData = this.projectData.getOrCreateClassData(this.myName);
        this.classData.setContainsInstrumentationInfo();

        // Do not attempt to instrument interfaces or classes that
        // have already been instrumented
        if (((access & Opcodes.ACC_INTERFACE) != 0)
                || arrayContains(interfaces, hasBeenInstrumented))
        {
            super.visit(version, access, name, signature, superName,
                    interfaces);
        }
        else
        {
            instrument = true;

            // Flag this class as having been instrumented
            String[] newInterfaces = new String[interfaces.length + 1];
            System.arraycopy(interfaces, 0, newInterfaces, 0,
                    interfaces.length);
            newInterfaces[newInterfaces.length - 1] = hasBeenInstrumented;
            if (signature != null) {
                signature = signature + "L" + hasBeenInstrumented + ";";
            }

            super.visit(version, access, name, signature, superName,
                    newInterfaces);
        }
    }

    /**
     * @param source In the format "ClassInstrumenter.java"
     */
    public void visitSource(String source, String debug)
    {
        super.visitSource(source, debug);
        classData.setSourceFileName(source);
    }

    public MethodVisitor visitMethod(final int access, final String name,
                                     final String desc, final String signature,
                                     final String[] exceptions)
    {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
                exceptions);

        if (!instrument)
            return mv;

        return mv == null ? null : new FirstPassMethodInstrumenter(classData, mv,
                this.myName, access, name, desc, signature, exceptions, ignoreRegexs,
                ignoreBranchesRegexs);
    }

    public void visitEnd()
    {
        if (instrument && classData.getNumberOfValidLines() == 0)
            logger.warn("No line number information found for class "
                    + this.myName
                    + ".  Perhaps you need to compile with debug=true?");
    }

}