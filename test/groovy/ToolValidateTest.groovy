import org.apache.commons.exec.*
import hudson.AbortException
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.RuleChain
import org.junit.rules.TemporaryFolder

import com.lesfurets.jenkins.unit.BasePipelineTest

import util.JenkinsLoggingRule
import util.Rules

class ToolValidateTest extends BasePipelineTest {

    private ExpectedException thrown = new ExpectedException().none()
    private TemporaryFolder tmp = new TemporaryFolder()
    private JenkinsLoggingRule jlr = new JenkinsLoggingRule(this)

    @Rule
    public RuleChain ruleChain = Rules.getCommonRules(this)
                                      .around(tmp)
                                      .around(thrown)
                                      .around(jlr)


    private notEmptyDir

    def toolValidateScript

    @Before
    void init() {

        notEmptyDir = tmp.newFolder('notEmptyDir')
        def path = "${notEmptyDir.getAbsolutePath()}${File.separator}test.txt"
        File file = new File(path)
        file.createNewFile()

        binding.setVariable('JAVA_HOME', notEmptyDir.getAbsolutePath())

        toolValidateScript =  loadScript("toolValidate.groovy").toolValidate
    }


    @Test
    void nullHomeTest() {

        thrown.expect(IllegalArgumentException)
        thrown.expectMessage("The parameter 'home' can not be null or empty.")

        toolValidateScript.call(tool: 'java', home: null)
    }

    @Test
    void emptyHomeTest() {

        thrown.expect(IllegalArgumentException)
        thrown.expectMessage("The parameter 'home' can not be null or empty.")

        toolValidateScript.call(tool: 'java', home: '')
    }

    @Test
    void nullToolTest() {

        thrown.expect(IllegalArgumentException)
        thrown.expectMessage("The parameter 'tool' can not be null or empty.")

        toolValidateScript.call(tool: null)
    }

    @Test
    void emptyToolTest() {

        thrown.expect(IllegalArgumentException)
        thrown.expectMessage("The parameter 'tool' can not be null or empty.")

        toolValidateScript.call(tool: '')
    }

    @Test
    void invalidToolTest() {

        thrown.expect(AbortException)
        thrown.expectMessage("The tool 'test' is not supported.")

        toolValidateScript.call(tool: 'test', home: notEmptyDir.getAbsolutePath())
    }

    @Test
    void unableToValidateJavaTest() {

        thrown.expect(AbortException)
        thrown.expectMessage('The validation of Java failed.')

        helper.registerAllowedMethod('sh', [Map], { Map m -> getNoVersion(m) })

        toolValidateScript.call(tool: 'java', home: notEmptyDir.getAbsolutePath())
    }

    @Test
    void unableToValidateMtaTest() {

        thrown.expect(AbortException)
        thrown.expectMessage('The validation of SAP Multitarget Application Archive Builder failed.')

        helper.registerAllowedMethod('sh', [Map], { Map m -> getNoVersion(m) })

        toolValidateScript.call(tool: 'mta', home: notEmptyDir.getAbsolutePath())
    }

    @Test
    void unableToValidateNeoTest() {

        thrown.expect(AbortException)
        thrown.expectMessage('The validation of SAP Cloud Platform Console Client failed.')

        helper.registerAllowedMethod('sh', [Map], { Map m -> getNoVersion(m) })

        toolValidateScript.call(tool: 'neo', home: notEmptyDir.getAbsolutePath())
    }

    @Test
    void unableToValidateCmTest() {

        thrown.expect(AbortException)
        thrown.expectMessage('The validation of Change Management Command Line Interface failed.')

        helper.registerAllowedMethod('sh', [Map], { Map m -> getNoVersion(m) })

        toolValidateScript.call(tool: 'cm', home: notEmptyDir.getAbsolutePath())

        script.execute()
    }

    @Test
    void validateIncompatibleVersionJavaTest() {

        thrown.expect(AbortException)
        thrown.expectMessage('The installed version of Java is 1.7.0.')

        helper.registerAllowedMethod('sh', [Map], { Map m -> getIncompatibleVersion(m) })

        toolValidateScript.call(tool: 'java', home: notEmptyDir.getAbsolutePath())
    }

    @Test
    void validateIncompatibleVersionMtaTest() {

        thrown.expect(AbortException)
        thrown.expectMessage('The installed version of SAP Multitarget Application Archive Builder is 1.0.5.')

        helper.registerAllowedMethod('sh', [Map], { Map m -> getIncompatibleVersion(m) })

        toolValidateScript.call(tool: 'mta', home: notEmptyDir.getAbsolutePath())
    }

    @Test
    void validateNeoIncompatibleVersionTest() {

        thrown.expect(AbortException)
        thrown.expectMessage('The installed version of SAP Cloud Platform Console Client is 1.126.51.')

        helper.registerAllowedMethod('sh', [Map], { Map m -> getIncompatibleVersion(m) })

        toolValidateScript.call(tool: 'neo', home: notEmptyDir.getAbsolutePath())
    }

    @Test
    void validateCmIncompatibleVersionTest() {

        thrown.expect(AbortException)
        thrown.expectMessage('The installed version of Change Management Command Line Interface is 0.0.0.')

        helper.registerAllowedMethod('sh', [Map], { Map m -> getIncompatibleVersion(m) })
        binding.setVariable('tool', 'cm')

        toolValidateScript.call(tool: 'cm', home: notEmptyDir.getAbsolutePath())
    }

    @Test
    void validateJavaTest() {

        helper.registerAllowedMethod('sh', [Map], { Map m -> getVersion(m) })

        toolValidateScript.call(tool: 'java', home: notEmptyDir.getAbsolutePath())

        assert jlr.log.contains('--- BEGIN LIBRARY STEP: toolValidate.groovy ---')
        assert jlr.log.contains('[INFO] Validating Java version 1.8.0 or compatible version.')
        assert jlr.log.contains('[INFO] Java version 1.8.0 is installed.')
        assert jlr.log.contains('--- END LIBRARY STEP: toolValidate.groovy ---')
    }

    @Test
    void validateMtaTest() {

        helper.registerAllowedMethod('sh', [Map], { Map m -> getVersion(m) })

        toolValidateScript.call(tool: 'mta', home: notEmptyDir.getAbsolutePath())

        assert jlr.log.contains('--- BEGIN LIBRARY STEP: toolValidate.groovy ---')
        assert jlr.log.contains('[INFO] Validating SAP Multitarget Application Archive Builder version 1.0.6 or compatible version.')
        assert jlr.log.contains('[INFO] SAP Multitarget Application Archive Builder version 1.0.6 is installed.')
        assert jlr.log.contains('--- END LIBRARY STEP: toolValidate.groovy ---')
    }

    @Test
    void validateNeoTest() {

        helper.registerAllowedMethod('sh', [Map], { Map m -> getVersion(m) })

        toolValidateScript.call(tool: 'neo', home: notEmptyDir.getAbsolutePath())

        assert jlr.log.contains('--- BEGIN LIBRARY STEP: toolValidate.groovy ---')
        assert jlr.log.contains('[INFO] Validating SAP Cloud Platform Console Client version 3.39.10 or compatible version.')
        assert jlr.log.contains('[INFO] SAP Cloud Platform Console Client version 3.39.10 is installed.')
        assert jlr.log.contains('--- END LIBRARY STEP: toolValidate.groovy ---')
    }

    @Test
    void validateCmTest() {

        helper.registerAllowedMethod('sh', [Map], { Map m -> getVersion(m) })

        toolValidateScript.call(tool: 'cm', home: notEmptyDir.getAbsolutePath())

        assert jlr.log.contains('--- BEGIN LIBRARY STEP: toolValidate.groovy ---')
        assert jlr.log.contains('[INFO] Validating Change Management Command Line Interface version 0.0.1 or compatible version.')
        assert jlr.log.contains('[INFO] Change Management Command Line Interface version 0.0.1 is installed.')
        assert jlr.log.contains('--- END LIBRARY STEP: toolValidate.groovy ---')
    }

    private getNoVersion(Map m) { 
        throw new AbortException('script returned exit code 127')
    }

    private getVersion(Map m) {

        if(m.script.contains('java -version')) {
            return '''openjdk version \"1.8.0_121\"
                    OpenJDK Runtime Environment (build 1.8.0_121-8u121-b13-1~bpo8+1-b13)
                    OpenJDK 64-Bit Server VM (build 25.121-b13, mixed mode)'''
        } else if(m.script.contains('mta.jar -v')) {
            return '1.0.6'
        } else if(m.script.contains('neo.sh version')) {
            return '''SAP Cloud Platform Console Client
                    SDK version    : 3.39.10
                    Runtime        : neo-java-web'''
        } else if(m.script.contains('cmclient -v')) {
            return '0.0.1-beta-2 : fc9729964a6acf5c1cad9c6f9cd6469727625a8e'
        }
    }

    private getIncompatibleVersion(Map m) {

        if(m.script.contains('java -version')) {
            return '''openjdk version \"1.7.0_121\"
                    OpenJDK Runtime Environment (build 1.7.0_121-8u121-b13-1~bpo8+1-b13)
                    OpenJDK 64-Bit Server VM (build 25.121-b13, mixed mode)'''
        } else if(m.script.contains('mta.jar -v')) {
            return '1.0.5'
        } else if(m.script.contains('neo.sh version')) {
            return '''SAP Cloud Platform Console Client
                    SDK version    : 1.126.51
                    Runtime        : neo-java-web'''
        } else if(m.script.contains('cmclient -v')) {
            return '0.0.0-beta-1 : fc9729964a6acf5c1cad9c6f9cd6469727625a8e'
        }
    }
}

