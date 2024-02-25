/*
 * Copyright 2012-2022 the original author or authors.
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

package org.springframework.boot.jarmode.layertools;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * The {@code 'list'} tools command.
 *
 * @author Phillip Webb
 */
class ListCommand extends Command {

	private final Context context;

	/**
	 * Constructor for ListCommand class.
	 * @param context the context object
	 */
	ListCommand(Context context) {
		super("list", "List layers from the jar that can be extracted", Options.none(), Parameters.none());
		this.context = context;
	}

	/**
	 * Runs the ListCommand with the given options and parameters.
	 * @param options a map of options for the command
	 * @param parameters a list of parameters for the command
	 */
	@Override
	protected void run(Map<Option, String> options, List<String> parameters) {
		printLayers(Layers.get(this.context), System.out);
	}

	/**
	 * Prints the layers in the given Layers object to the specified PrintStream.
	 * @param layers the Layers object containing the layers to be printed
	 * @param out the PrintStream to which the layers will be printed
	 */
	void printLayers(Layers layers, PrintStream out) {
		layers.forEach(out::println);
	}

}
