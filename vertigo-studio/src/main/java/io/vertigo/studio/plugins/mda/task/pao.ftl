<#import "macro_ao.ftl" as lib>
package ${pao.packageName};

import javax.inject.Inject;

<#if pao.options >
import io.vertigo.lang.Option;
</#if>
import io.vertigo.core.Home;
import io.vertigo.lang.Assertion;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;

/**
 * PAO : Accès aux objects du package. 
 * ${pao.classSimpleName}
 */
public final class ${pao.classSimpleName} {
	private final TaskManager taskManager;

	/**
	 * Constructeur.
	 * @param taskManager Manager des Task
	 */
	@Inject
	public ${pao.classSimpleName}(final TaskManager taskManager) {
		Assertion.checkNotNull(taskManager);
		//---------------------------------------------------------------------
		this.taskManager = taskManager;
	}
    <@lib.generateBody pao.taskDefinitions/> 
    
    private TaskManager getTaskManager(){
    	return taskManager;
    } 
}
