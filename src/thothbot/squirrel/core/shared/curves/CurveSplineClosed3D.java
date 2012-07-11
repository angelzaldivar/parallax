/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 * 
 * This file based on the JavaScript source file of the THREE.JS project, 
 * licensed under MIT License.
 * 
 * This file is part of Squirrel project.
 * 
 * Squirrel is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 * 
 * Squirrel is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with 
 * Squirrel. If not, see http://www.gnu.org/licenses/.
 */

package thothbot.squirrel.core.shared.curves;

import java.util.ArrayList;
import java.util.List;

import thothbot.squirrel.core.shared.core.Vector3f;
import thothbot.squirrel.core.shared.utils.CurveUtils;

public class CurveSplineClosed3D extends Curve
{
	public List<Vector3f> points;
	
	public CurveSplineClosed3D() 
	{
		this.points = new ArrayList<Vector3f>();
	}

	public CurveSplineClosed3D(List<Vector3f> points) 
	{
		this.points = points;
	}

	@Override
	public Vector3f getPoint(float t)
	{
		Vector3f v = new Vector3f();
		
		 // This needs to be from 0-length +1
		float point = ( points.size() - 0 ) * t;
		int intPoint = (int) Math.floor( point );
		
		float weight = point - intPoint;
		intPoint += intPoint > 0 ? 0 : ( Math.floor( Math.abs( intPoint ) / points.size() ) + 1 ) * points.size();
		
		int c0 = ( intPoint - 1 ) % points.size();
		int c1 = ( intPoint ) % points.size();
		int c2 = ( intPoint + 1 ) % points.size();
		int c3 = ( intPoint + 2 ) % points.size();

		v.setX( CurveUtils.interpolate( points.get(c0).getX(), points.get(c1).getX(), points.get(c2).getX(), points.get(c3).getX(), weight) );
		v.setY( CurveUtils.interpolate( points.get(c0).getY(), points.get(c1).getY(), points.get(c2).getY(), points.get(c3).getY(), weight) );
		v.setZ( CurveUtils.interpolate( points.get(c0).getZ(), points.get(c1).getZ(), points.get(c2).getZ(), points.get(c3).getZ(), weight) );

		return v;
	}
}