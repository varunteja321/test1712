/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.cli.command.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.boot.cli.command.Command;
import org.springframework.boot.cli.command.options.OptionHelp;
import org.springframework.boot.cli.command.status.ExitStatus;
import org.springframework.boot.loader.tools.JavaExecutable;

/**
 * Decorate an existing command to run it by forking the current java process.
 *
 * @author Phillip Webb
 */
class ForkProcessCommand extends RunProcessCommand {

	private static final String MAIN_CLASS = "org.springframework.boot.loader.launch.JarLauncher";

	private final Command command;

	/**
	 * Creates a new instance of ForkProcessCommand with the specified command.
	 * @param command the command to be executed
	 */
	ForkProcessCommand(Command command) {
		super(new JavaExecutable().toString());
		this.command = command;
	}

	/**
	 * Returns the name of the command.
	 * @return the name of the command
	 */
	@Override
	public String getName() {
		return this.command.getName();
	}

	/**
	 * Returns the description of the command.
	 * @return the description of the command
	 */
	@Override
	public String getDescription() {
		return this.command.getDescription();
	}

	/**
	 * Returns the usage help for the ForkProcessCommand.
	 * @return the usage help for the ForkProcessCommand
	 */
	@Override
	public String getUsageHelp() {
		return this.command.getUsageHelp();
	}

	/**
	 * Returns the help information for the command.
	 * @return the help information for the command
	 */
	@Override
	public String getHelp() {
		return this.command.getHelp();
	}

	/**
	 * Returns the collection of option help for the command.
	 * @return the collection of option help
	 */
	@Override
	public Collection<OptionHelp> getOptionsHelp() {
		return this.command.getOptionsHelp();
	}

	/**
	 * Runs the specified command with the given arguments.
	 * @param args the arguments to be passed to the command
	 * @return the exit status of the command
	 * @throws Exception if an error occurs while running the command
	 */
	@Override
	public ExitStatus run(String... args) throws Exception {
		List<String> fullArgs = new ArrayList<>();
		fullArgs.add("-cp");
		fullArgs.add(System.getProperty("java.class.path"));
		fullArgs.add(MAIN_CLASS);
		fullArgs.add(this.command.getName());
		fullArgs.addAll(Arrays.asList(args));
		run(fullArgs);
		return ExitStatus.OK;
	}

}
