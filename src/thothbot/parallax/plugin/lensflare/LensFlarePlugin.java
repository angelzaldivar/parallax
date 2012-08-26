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
 * Squirrel. If not, see http://www.gnu.org/licenses/.
 */

package thothbot.parallax.plugin.lensflare;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.thirdparty.streamhtmlparser.HtmlParser.ATTR_TYPE;
import com.google.gwt.thirdparty.streamhtmlparser.HtmlParserFactory.AttributeOptions;

import thothbot.parallax.core.client.gl2.WebGLBuffer;
import thothbot.parallax.core.client.gl2.WebGLRenderingContext;
import thothbot.parallax.core.client.gl2.WebGLTexture;
import thothbot.parallax.core.client.gl2.arrays.Float32Array;
import thothbot.parallax.core.client.gl2.arrays.Uint16Array;
import thothbot.parallax.core.client.gl2.enums.GLenum;
import thothbot.parallax.core.client.renderers.Plugin;
import thothbot.parallax.core.client.renderers.WebGLRenderer;
import thothbot.parallax.core.client.shader.Attribute;
import thothbot.parallax.core.client.shader.Uniform;
import thothbot.parallax.core.shared.Log;
import thothbot.parallax.core.shared.cameras.Camera;
import thothbot.parallax.core.shared.core.FastMap;
import thothbot.parallax.core.shared.core.Vector2;
import thothbot.parallax.core.shared.core.Vector3;
import thothbot.parallax.core.shared.scenes.Scene;
import thothbot.parallax.plugin.lensflare.shader.ShaderLensFlare;
import thothbot.parallax.plugin.lensflare.shader.ShaderLensFlareVertexTexture;

public final class LensFlarePlugin extends Plugin
{
	public class LensFlareGeometry 
	{
		Float32Array vertices;
		Uint16Array faces;
		
		WebGLBuffer vertexBuffer;
		WebGLBuffer elementBuffer;
		
		WebGLTexture tempTexture;
		WebGLTexture occlusionTexture;
		
		ShaderLensFlare shader;
//		WebGLProgram program;
//		Map<String, Integer> attributes;
//		Map<String, WebGLUniformLocation> uniforms;
		
		boolean hasVertexTexture;
		boolean attributesEnabled;
	}
	
	private LensFlareGeometry lensFlare;
	
	@Override
	public void init(WebGLRenderer webGLRenderer) 
	{
		this.renderer = webGLRenderer;
		this.lensFlare = new LensFlareGeometry();
		
		WebGLRenderingContext gl = this.renderer.getGL();
		

		lensFlare.vertices = Float32Array.create( 8 + 8 );
		lensFlare.faces = Uint16Array.create( 6 );

		int i = 0;
		lensFlare.vertices.set( i++, -1); lensFlare.vertices.set( i++, -1);	// vertex
		lensFlare.vertices.set( i++, 0);  lensFlare.vertices.set( i++, 0);	// uv... etc.

		lensFlare.vertices.set( i++, 1);  lensFlare.vertices.set( i++, -1);
		lensFlare.vertices.set( i++, 1);  lensFlare.vertices.set( i++, 0);

		lensFlare.vertices.set( i++, 1);  lensFlare.vertices.set( i++, 1);
		lensFlare.vertices.set( i++, 1);  lensFlare.vertices.set( i++, 1);

		lensFlare.vertices.set( i++, -1); lensFlare.vertices.set( i++, 1);
		lensFlare.vertices.set( i++, 0);  lensFlare.vertices.set( i++, 1);

		i = 0;
		lensFlare.faces.set( i++, 0); lensFlare.faces.set( i++, 1); lensFlare.faces.set( i++, 2);
		lensFlare.faces.set( i++, 0); lensFlare.faces.set( i++, 2); lensFlare.faces.set( i++, 3);

		// buffers

		lensFlare.vertexBuffer     = gl.createBuffer();
		lensFlare.elementBuffer    = gl.createBuffer();

		gl.bindBuffer( GLenum.ARRAY_BUFFER.getValue(), lensFlare.vertexBuffer );
		gl.bufferData( GLenum.ARRAY_BUFFER.getValue(), lensFlare.vertices, GLenum.STATIC_DRAW.getValue() );

		gl.bindBuffer( GLenum.ELEMENT_ARRAY_BUFFER.getValue(), lensFlare.elementBuffer );
		gl.bufferData( GLenum.ELEMENT_ARRAY_BUFFER.getValue(), lensFlare.faces, GLenum.STATIC_DRAW.getValue() );

		// textures

		lensFlare.tempTexture      = gl.createTexture();
		lensFlare.occlusionTexture = gl.createTexture();

		gl.bindTexture( GLenum.TEXTURE_2D.getValue(), lensFlare.tempTexture );
		gl.texImage2D( GLenum.TEXTURE_2D.getValue(), 0, GLenum.RGB.getValue(), 16, 16, 0, GLenum.RGB.getValue(), GLenum.UNSIGNED_BYTE.getValue(), null );
		gl.texParameteri( GLenum.TEXTURE_2D.getValue(), GLenum.TEXTURE_WRAP_S.getValue(), GLenum.CLAMP_TO_EDGE.getValue() );
		gl.texParameteri( GLenum.TEXTURE_2D.getValue(), GLenum.TEXTURE_WRAP_T.getValue(), GLenum.CLAMP_TO_EDGE.getValue() );
		gl.texParameteri( GLenum.TEXTURE_2D.getValue(), GLenum.TEXTURE_MAG_FILTER.getValue(), GLenum.NEAREST.getValue() );
		gl.texParameteri( GLenum.TEXTURE_2D.getValue(), GLenum.TEXTURE_MIN_FILTER.getValue(), GLenum.NEAREST.getValue() );

		gl.bindTexture( GLenum.TEXTURE_2D.getValue(), lensFlare.occlusionTexture );
		gl.texImage2D( GLenum.TEXTURE_2D.getValue(), 0, GLenum.RGBA.getValue(), 16, 16, 0, GLenum.RGBA.getValue(), GLenum.UNSIGNED_BYTE.getValue(), null );
		gl.texParameteri( GLenum.TEXTURE_2D.getValue(), GLenum.TEXTURE_WRAP_S.getValue(), GLenum.CLAMP_TO_EDGE.getValue() );
		gl.texParameteri( GLenum.TEXTURE_2D.getValue(), GLenum.TEXTURE_WRAP_T.getValue(), GLenum.CLAMP_TO_EDGE.getValue() );
		gl.texParameteri( GLenum.TEXTURE_2D.getValue(), GLenum.TEXTURE_MAG_FILTER.getValue(), GLenum.NEAREST.getValue() );
		gl.texParameteri( GLenum.TEXTURE_2D.getValue(), GLenum.TEXTURE_MIN_FILTER.getValue(), GLenum.NEAREST.getValue() );

		if ( gl.getParameteri( GLenum.MAX_VERTEX_TEXTURE_IMAGE_UNITS.getValue() ) <= 0 ) 
		{
			lensFlare.hasVertexTexture = false;
			lensFlare.shader = new ShaderLensFlare();
		} 
		else 
		{
			lensFlare.hasVertexTexture = true;
			lensFlare.shader = new ShaderLensFlareVertexTexture();
		}

		Map<String, Attribute> attributes = GWT.isScript() ? 
				new FastMap<Attribute>() : new HashMap<String, Attribute>();
		attributes.put("vertex", new Attribute(Attribute.TYPE.V3, null));
		lensFlare.shader.setAttributes(attributes);
		lensFlare.shader.buildProgram(gl);
	}


	/**
	 * Render lens flares
	 * Method: renders 16x16 0xff00ff-colored points scattered over the light source area,
	 *         reads these back and calculates occlusion.
	 *         Then _lensFlare.update_lensFlares() is called to re-position and
	 *         update transparency of flares. Then they are rendered.
	 *
	 */
	@Override
	public void render(Scene scene, Camera camera, int viewportWidth, int viewportHeight) 
	{
		List<LensFlare> flares = scene.__webglFlares;
		int nFlares = flares.size();

		if ( nFlares == 0 ) return;

		WebGLRenderingContext gl = this.renderer.getGL();

		Vector3 tempPosition = new Vector3();

		double invAspect = (double)viewportHeight / viewportWidth;
		double halfViewportWidth = viewportWidth * 0.5;
		double halfViewportHeight = viewportHeight * 0.5;

		double size = 16.0 / viewportHeight;
		Vector2 scale = new Vector2( size * invAspect, size );

		Vector3 screenPosition = new Vector3( 1, 1, 0 );
		Vector2 screenPositionPixels = new Vector2( 1, 1 );

		Map<String, Uniform> uniforms = this.lensFlare.shader.getUniforms();
		Map<String, Integer> attributesLocation = this.lensFlare.shader.getAttributesLocations();

		// set _lensFlare program and reset blending

		gl.useProgram( lensFlare.shader.getProgram() );

		if ( ! lensFlare.attributesEnabled ) 
		{
			gl.enableVertexAttribArray( attributesLocation.get("vertex") );
			gl.enableVertexAttribArray( attributesLocation.get("uv") );

			lensFlare.attributesEnabled = true;
		}

		// loop through all lens flares to update their occlusion and positions
		// setup gl and common used attribs/unforms

		gl.uniform1i( uniforms.get("occlusionMap").getLocation(), 0 );
		gl.uniform1i( uniforms.get("map").getLocation(), 1 );

		gl.bindBuffer( GLenum.ARRAY_BUFFER.getValue(), lensFlare.vertexBuffer );
		gl.vertexAttribPointer( attributesLocation.get("vertex"), 2, GLenum.FLOAT.getValue(), false, 2 * 8, 0 );
		gl.vertexAttribPointer( attributesLocation.get("uv"), 2, GLenum.FLOAT.getValue(), false, 2 * 8, 8 );

		gl.bindBuffer( GLenum.ELEMENT_ARRAY_BUFFER.getValue(), lensFlare.elementBuffer );

		gl.disable( GLenum.CULL_FACE.getValue() );
		gl.depthMask( false );

		for ( int i = 0; i < nFlares; i ++ ) 
		{
			size = 16.0 / viewportHeight;
			scale.set( size * invAspect, size );

			// calc object screen position

			LensFlare flare = flares.get( i );

			tempPosition.set( 
					flare.getMatrixWorld().getArray().get(12), 
					flare.getMatrixWorld().getArray().get(13), 
					flare.getMatrixWorld().getArray().get(14) );

			camera.getMatrixWorldInverse().multiplyVector3( tempPosition );
			camera.getProjectionMatrix().multiplyVector3( tempPosition );

			// setup arrays for gl programs

			screenPosition.copy( tempPosition );

			screenPositionPixels.setX( screenPosition.getX() * halfViewportWidth + halfViewportWidth);
			screenPositionPixels.setY( screenPosition.getY() * halfViewportHeight + halfViewportHeight);

			// screen cull

			if ( lensFlare.hasVertexTexture || (
					screenPositionPixels.getX() > 0 &&
					screenPositionPixels.getX() < viewportWidth &&
					screenPositionPixels.getY() > 0 &&
					screenPositionPixels.getY() < viewportHeight ) 
			) {

				// save current RGB to temp texture

				gl.activeTexture( GLenum.TEXTURE1.getValue() );
				gl.bindTexture( GLenum.TEXTURE_2D.getValue(), lensFlare.tempTexture );
				gl.copyTexImage2D( GLenum.TEXTURE_2D.getValue(), 0, GLenum.RGB.getValue(), (int)screenPositionPixels.getX() - 8, (int)screenPositionPixels.getY() - 8, 16, 16, 0 );

				// render pink quad

				gl.uniform1i( uniforms.get("renderType").getLocation(), 0 );
				gl.uniform2f( uniforms.get("scale").getLocation(), scale.getX(), scale.getY() );
				gl.uniform3f( uniforms.get("screenPosition").getLocation(), screenPosition.getX(), screenPosition.getY(), screenPosition.getZ() );

				gl.disable( GLenum.BLEND.getValue() );
				gl.enable( GLenum.DEPTH_TEST.getValue() );

				gl.drawElements( GLenum.TRIANGLES.getValue(), 6, GLenum.UNSIGNED_SHORT.getValue(), 0 );

				// copy result to occlusionMap

				gl.activeTexture( GLenum.TEXTURE0.getValue() );
				gl.bindTexture( GLenum.TEXTURE_2D.getValue(), lensFlare.occlusionTexture );
				gl.copyTexImage2D( GLenum.TEXTURE_2D.getValue(), 0, GLenum.RGBA.getValue(), (int)screenPositionPixels.getX() - 8, (int)screenPositionPixels.getY() - 8, 16, 16, 0 );

				// restore graphics

				gl.uniform1i( uniforms.get("renderType").getLocation(), 1 );
				gl.disable( GLenum.DEPTH_TEST.getValue() );

				gl.activeTexture( GLenum.TEXTURE1.getValue() );
				gl.bindTexture( GLenum.TEXTURE_2D.getValue(), lensFlare.tempTexture );
				gl.drawElements( GLenum.TRIANGLES.getValue(), 6, GLenum.UNSIGNED_SHORT.getValue(), 0 );

				// update object positions

				flare.getPositionScreen().copy( screenPosition );

				flare.getUpdateCallback().update();

				// render flares

				gl.uniform1i( uniforms.get("renderType").getLocation(), 2 );
				gl.enable( GLenum.BLEND.getValue() );

				for ( int j = 0, jl = flare.getLensFlares().size(); j < jl; j ++ ) 
				{
					LensFlare.LensSprite sprite = flare.getLensFlares().get( j );

					if ( sprite.opacity > 0.001 && sprite.scale > 0.001 ) 
					{
						screenPosition.setX( sprite.x );
						screenPosition.setY( sprite.y );
						screenPosition.setZ( sprite.z );

						size = sprite.size * sprite.scale / viewportHeight;

						scale.setX( size * invAspect );
						scale.setY( size );

						gl.uniform3f( uniforms.get("screenPosition").getLocation(), screenPosition.getX(), screenPosition.getY(), screenPosition.getZ() );
						gl.uniform2f( uniforms.get("scale").getLocation(), scale.getX(), scale.getY() );
						gl.uniform1f( uniforms.get("rotation").getLocation(), sprite.rotation );

						gl.uniform1f( uniforms.get("opacity").getLocation(), sprite.opacity );
						gl.uniform3f( uniforms.get("color").getLocation(), sprite.color.getR(), sprite.color.getG(), sprite.color.getB() );

//						renderer.setBlending( sprite.blending, sprite.blendEquation, sprite.blendSrc, sprite.blendDst );
						renderer.setBlending( sprite.blending );
						renderer.setTexture( sprite.texture, 1 );

						gl.drawElements( GLenum.TRIANGLES.getValue(), 6, GLenum.UNSIGNED_SHORT.getValue(), 0 );
					}
				}
			}
		}

		// restore gl

		gl.enable( GLenum.CULL_FACE.getValue() );
		gl.enable( GLenum.DEPTH_TEST.getValue() );
		gl.depthMask( true );

	}
}