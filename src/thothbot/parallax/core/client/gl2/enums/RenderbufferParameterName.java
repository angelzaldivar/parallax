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

public enum RenderbufferParameterName implements GLEnum
{
	/** 
	 * Returns int 
	 */
	RENDERBUFFER_WIDTH(WebGLConstants.RENDERBUFFER_WIDTH),
	
	/** 
	 * Returns int 
	 */
	RENDERBUFFER_HEIGHT(WebGLConstants.RENDERBUFFER_HEIGHT),
	
	/** 
	 * Returns int 
	 */
	RENDERBUFFER_INTERNAL_FORMAT(WebGLConstants.RENDERBUFFER_INTERNAL_FORMAT),
	
	/** 
	 * Returns int 
	 */
	RENDERBUFFER_RED_SIZE(WebGLConstants.RENDERBUFFER_RED_SIZE),
	
	/** 
	 * Returns int 
	 */
	RENDERBUFFER_GREEN_SIZE(WebGLConstants.RENDERBUFFER_GREEN_SIZE),
	
	/** 
	 * Returns int 
	 */
	RENDERBUFFER_BLUE_SIZE(WebGLConstants.RENDERBUFFER_BLUE_SIZE),
	
	/** 
	 * Returns int 
	 */
	RENDERBUFFER_ALPHA_SIZE(WebGLConstants.RENDERBUFFER_ALPHA_SIZE),
	
	/** 
	 * Returns int 
	 */
	RENDERBUFFER_DEPTH_SIZE(WebGLConstants.RENDERBUFFER_DEPTH_SIZE),
	
	/** 
	 * Returns int 
	 */
	RENDERBUFFER_STENCIL_SIZE(WebGLConstants.RENDERBUFFER_STENCIL_SIZE);

	private final int value;

	private RenderbufferParameterName(int value) {
		this.value = value;
	}

	@Override
	public int getValue() {
		return value;
	}
}
