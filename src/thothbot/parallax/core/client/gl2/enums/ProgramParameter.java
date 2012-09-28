/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 * 
 * This file is part of Parallax project.
 * 
 * Parallax is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 * 
 * Parallax is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * Parallax. If not, see http://www.gnu.org/licenses/.
 */

package thothbot.parallax.core.client.gl2.enums;

import thothbot.parallax.core.client.gl2.WebGLConstants;

public enum ProgramParameter implements GLEnum
{
	/** 
	 * Returns boolean 
	 */
	DELETE_STATUS(WebGLConstants.DELETE_STATUS),
	
	/** 
	 * Returns boolean
	 */
	LINK_STATUS(WebGLConstants.LINK_STATUS),
	
	/** 
	 * Returns boolean 
	 */
	VALIDATE_STATUS(WebGLConstants.VALIDATE_STATUS),
	
	/** 
	 * Returns int 
	 */
	INFO_LOG_LENGTH(WebGLConstants.INFO_LOG_LENGTH),
	
	/** 
	 * Returns int 
	 */
	ATTACHED_SHADERS(WebGLConstants.ATTACHED_SHADERS),
	
	/** 
	 * Returns int 
	 */
	ACTIVE_UNIFORMS(WebGLConstants.ACTIVE_UNIFORMS),
	
	/** 
	 * Returns int 
	 */
	ACTIVE_UNIFORM_MAX_LENGTH(WebGLConstants.ACTIVE_UNIFORM_MAX_LENGTH),
	
	/** 
	 * Returns int 
	 */
	ACTIVE_ATTRIBUTES(WebGLConstants.ACTIVE_ATTRIBUTES),
	
	/** 
	 * Returns int 
	 */
	ACTIVE_ATTRIBUTE_MAX_LENGTH(WebGLConstants.ACTIVE_ATTRIBUTE_MAX_LENGTH);

	private final int value;

	private ProgramParameter(int value) {
		this.value = value;
	}

	@Override
	public int getValue() {
		return value;
	}
}
