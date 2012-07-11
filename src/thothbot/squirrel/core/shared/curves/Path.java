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
import java.util.Arrays;
import java.util.List;

import thothbot.squirrel.core.shared.Log;
import thothbot.squirrel.core.shared.core.Vector2f;
import thothbot.squirrel.core.shared.utils.ShapeUtils;

public class Path extends CurvePath
{
	public static enum PATH_ACTIONS {
		MOVE_TO,
		LINE_TO,
		QUADRATIC_CURVE_TO, // Bezier quadratic curve
		BEZIER_CURVE_TO, 	// Bezier cubic curve
		CSPLINE_THRU,		// Catmull-rom spline
		ARC					// Circle
	};
	
	public class Action 
	{
		public PATH_ACTIONS action;
		public List<Object> args;
		
		public Action(PATH_ACTIONS action, Object ...args)
		{
			this.action = action;
			this.args = Arrays.asList(args);
		}
		
		public String toString()
		{
			return "{action=" + this.action.name() + ", args=" + this.args + "}";
		}
	}
	
	private List<Action> actions;
	private boolean useSpacedPoints = false;

	public Path() 
	{
		super();
		this.actions = new ArrayList<Path.Action>();
	}
	
	public Path( List<Vector2f> points ) 
	{
		this();
		this.fromPoints( points );
	}
	
	public List<Action> getActions()
	{
		return this.actions;
	}
	
	// Create path using straight lines to connect all points
	// - vectors: array of Vector2

	public void fromPoints( List<Vector2f> vectors ) 
	{
		moveTo( vectors.get( 0 ).getX(), vectors.get( 0 ).getY() );

		for ( int v = 1, vlen = vectors.size(); v < vlen; v ++ )
			lineTo( vectors.get( v ).getX(), vectors.get( v ).getY() );
	}
	
	public void moveTo( float x, float y ) 
	{
		this.actions.add( new Action(PATH_ACTIONS.MOVE_TO, x, y));
	}
	
	public void lineTo( float x, float y ) 
	{	
		List<Object> lastargs = this.actions.get( this.actions.size() - 1 ).args;

		float x0 = (Float) lastargs.get( lastargs.size() - 2 );
		float y0 = (Float) lastargs.get( lastargs.size() - 1 );

		CurveLine curve = new CurveLine( new Vector2f( x0, y0 ), new Vector2f( x, y ) );
		add(curve);

		this.actions.add( new Action( PATH_ACTIONS.LINE_TO, x, y ) );
	}
	
	public void quadraticCurveTo(float aCPx, float aCPy, float aX, float aY ) 
	{		
		List<Object> lastargs = this.actions.get( this.actions.size() - 1 ).args;
				
		float x0 = (Float) lastargs.get( lastargs.size() - 2 );
		float y0 = (Float) lastargs.get( lastargs.size() - 1 );

		CurveQuadraticBezier curve = new CurveQuadraticBezier( 
				new Vector2f( x0, y0 ),
				new Vector2f( aCPx, aCPy ),
				new Vector2f( aX, aY ) );
		add(curve);

		this.actions.add( new Action( PATH_ACTIONS.QUADRATIC_CURVE_TO, aCPx, aCPy, aX, aY ) );
	}
	
	public void bezierCurveTo( float aCP1x, float aCP1y, float aCP2x, float aCP2y, float aX, float aY ) 
	{
	
		List<Object> lastargs = this.actions.get( this.actions.size() - 1 ).args;

		float x0 = (Float) lastargs.get( lastargs.size() - 2 );
		float y0 = (Float) lastargs.get( lastargs.size() - 1 );

		CurveCubicBezier curve = new CurveCubicBezier( new Vector2f( x0, y0 ),
				new Vector2f( aCP1x, aCP1y ),
				new Vector2f( aCP2x, aCP2y ),
				new Vector2f( aX, aY ) );
		add( curve );

		this.actions.add( new Action( PATH_ACTIONS.BEZIER_CURVE_TO, aCP1x, aCP1y, aCP2x, aCP2y, aX, aY ) );
	}
	
	/*
	 * @param pts
	 * 		List<{@link Vector2f}>
	 */
	public void splineThru( List<Vector2f> pts) 
	{	
		List<Object> lastargs = this.actions.get( this.actions.size() - 1 ).args;

		float x0 = (Float) lastargs.get( lastargs.size() - 2 );
		float y0 = (Float) lastargs.get( lastargs.size() - 1 );
		
		//---
		List<Vector2f> npts = Arrays.asList( new Vector2f( x0, y0 ) );
		npts.addAll(pts);
	
		CurveSpline curve = new CurveSpline( npts );
		add( curve );

		this.actions.add( new Action(PATH_ACTIONS.CSPLINE_THRU, pts ) );
	}
	
	/*
	 * FUTURE: Change the API or follow canvas API?
	 * TODO ARC ( x, y, x - radius, y - radius, startAngle, endAngle )
	 */
	public void arc( float aX, float aY, float aRadius, float aStartAngle, float aEndAngle, boolean aClockwise ) 
	{
		List<Object> laste = this.actions.get( this.actions.size() - 1 ).args;
		
		CurveArc curve = new CurveArc( (Float)laste.get(0) + aX, (Float)laste.get(1) + aY, aRadius,
				aStartAngle, aEndAngle, aClockwise );
		add( curve );

		// All of the other actions look to the last two elements in the list to
		// find the ending point, so we need to append them.
		Vector2f lastPoint = curve.getPoint(aClockwise ? 1 : 0);

		this.actions.add( new Action(PATH_ACTIONS.ARC, aX, aY, aRadius, aStartAngle, aEndAngle, aClockwise, lastPoint.getX(), lastPoint.getY() ) );
	}

	public void absarc( float aX, float aY, float aRadius, float aStartAngle, float aEndAngle, boolean aClockwise ) 
	{		
			CurveArc curve = new CurveArc( aX, aY, aRadius,
					aStartAngle, aEndAngle, aClockwise );
			add( curve );

			// All of the other actions look to the last two elements in the list to
			// find the ending point, so we need to append them.
			Vector2f lastPoint = curve.getPoint(aClockwise ? 1 : 0);

			this.actions.add( new Action(PATH_ACTIONS.ARC, aX, aY, aRadius, aStartAngle, aEndAngle, aClockwise, lastPoint.getX(), lastPoint.getY() ) );
	}
	
	public List<Vector2f> getSpacedPoints( boolean closedPath )
	{
		return getSpacedPoints(40, closedPath);
	}

	public List<Vector2f> getSpacedPoints( int divisions, boolean closedPath ) 
	{

		List<Vector2f> points = new ArrayList<Vector2f>();

		for ( int i = 0; i < divisions; i ++ )
			points.add( this.getPoint( i / divisions ) );

		// if ( closedPath ) {
		//
		// 	points.push( points[ 0 ] );
		//
		// }

		return points;
	}
	
	/* 
	 * @return an List of {@link Vector2f} based on contour of the path
	 */
	public List<Vector2f> getPoints( boolean closedPath ) 
	{
		return getPoints(12, closedPath);
	}

	public List<Vector2f> getPoints( int divisions, boolean closedPath ) 
	{
		Log.debug("Called Path:getPoins()");

		if (this.useSpacedPoints)
			return this.getSpacedPoints( divisions, closedPath );

		List<Vector2f> points = new ArrayList<Vector2f>();

		float cpx, cpy, cpx2, cpy2, cpx1, cpy1, cpx0, cpy0;
		
//		var i, il, item, action, args;
//		var cpx, cpy, cpx2, cpy2, cpx1, cpy1, cpx0, cpy0,
//			laste, j,
//			t, tx, ty;

		for ( int i = 0, il = this.actions.size(); i < il; i ++ ) 
		{
			Action item = this.actions.get( i );

			PATH_ACTIONS action = item.action;
			List<Object> args = item.args;

			switch( action ) 
			{

			case MOVE_TO:

				points.add( new Vector2f( (Float)args.get( 0 ), (Float)args.get( 1 ) ) );
				break;

			case LINE_TO:

				points.add( new Vector2f( (Float)args.get( 0 ), (Float)args.get( 1 ) ) );
				break;

			case QUADRATIC_CURVE_TO:

				cpx  = (Float)args.get( 2 );
				cpy  = (Float)args.get( 3 );

				cpx1 = (Float)args.get( 0 );
				cpy1 = (Float)args.get( 1 );

				if ( points.size() > 0 ) 
				{
					Vector2f laste = points.get( points.size() - 1 );

					cpx0 = laste.getX();
					cpy0 = laste.getY();
				} 
				else 
				{

					List<Object> laste = this.actions.get( i - 1 ).args;

					cpx0 = (Float) laste.get( laste.size() - 2 );
					cpy0 = (Float) laste.get( laste.size() - 1 );

				}

				for ( int j = 1; j <= divisions; j ++ ) 
				{
					float t = (float)j / divisions;

					float tx = ShapeUtils.b2( t, cpx0, cpx1, cpx );
					float ty = ShapeUtils.b2( t, cpy0, cpy1, cpy );

					points.add( new Vector2f( tx, ty ) );

			  	}

				break;

			case BEZIER_CURVE_TO:

				cpx  = (Float)args.get( 4 );
				cpy  = (Float)args.get( 5 );

				cpx1 = (Float)args.get( 0 );
				cpy1 = (Float)args.get( 1 );

				cpx2 = (Float)args.get( 2 );
				cpy2 = (Float)args.get( 3 );

				if ( points.size() > 0 ) 
				{
					Vector2f laste = points.get( points.size() - 1 );

					cpx0 = laste.getX();
					cpy0 = laste.getY();
				} 
				else 
				{

					List<Object> laste = this.actions.get( i - 1 ).args;

					cpx0 = (Float) laste.get( laste.size() - 2 );
					cpy0 = (Float) laste.get( laste.size() - 1 );
				}


				for ( int j = 1; j <= divisions; j ++ ) 
				{
					float t = (float)j / divisions;

					float tx = ShapeUtils.b3( t, cpx0, cpx1, cpx2, cpx );
					float ty = ShapeUtils.b3( t, cpy0, cpy1, cpy2, cpy );

					points.add( new Vector2f( tx, ty ) );
				}

				break;

			case CSPLINE_THRU:

				List<Object> laste = this.actions.get( i - 1 ).args;

				Vector2f last = new Vector2f( (Float)laste.get( laste.size() - 2 ), (Float)laste.get( laste.size() - 1 ) );
				List<Vector2f> spts = Arrays.asList(last);

				Vector2f v = (Vector2f) args.get( 0 );
				float n = divisions * v.length();

				spts.add(v);

				CurveSpline spline = new CurveSpline( spts );

				for ( int j = 1; j <= n; j ++ )
					points.add( (Vector2f) spline.getPointAt( j / n ) ) ;

				break;

				// TODO: Fix
//			case ARC:
//
//				List<Objcet>laste = this.actions.get( i - 1 ).args;
//
//				var aX = args[ 0 ], aY = args[ 1 ],
//					aRadius = args[ 2 ],
//					aStartAngle = args[ 3 ], aEndAngle = args[ 4 ],
//					aClockwise = !!args[ 5 ];
//
//
//				var deltaAngle = aEndAngle - aStartAngle;
//				var angle;
//				var tdivisions = divisions * 2;
//
//				for ( j = 1; j <= tdivisions; j ++ ) {
//
//					t = j / tdivisions;
//
//					if ( ! aClockwise ) {
//
//						t = 1 - t;
//
//					}
//
//					angle = aStartAngle + t * deltaAngle;
//
//					tx = aX + aRadius * Math.cos( angle );
//					ty = aY + aRadius * Math.sin( angle );
//
//					//console.log('t', t, 'angle', angle, 'tx', tx, 'ty', ty);
//
//					points.push( new THREE.Vector2( tx, ty ) );
//
//				}
//
//				//console.log(points);
//
//			  break;
//
			} // end switch

		}



		// Normalize to remove the closing point by default.
		Vector2f lastPoint = points.get( points.size() - 1);
		double EPSILON = 0.0000000001;
		if ( Math.abs(lastPoint.getX() - points.get( 0 ).getX()) < EPSILON &&
	             Math.abs(lastPoint.getY() - points.get( 0 ).getY()) < EPSILON)
			points.remove( points.size() - 1);
		
		if ( closedPath )
			points.add( points.get( 0 ) );

		return points;
	}
	
	/*
	 * Read http://www.tinaja.com/glib/nonlingr.pdf
	 * nonlinear transforms
	 * @param a horizontal size
	 * @param b lean
	 * @param c X-offset
	 * @param d vertical size
	 * @param e climb
	 * @param f Y-offset
	 */
//	public List<Vector2f> nltransform( float a, float b, float c, float d, float e, float f ) 
//	{
//		List<Vector2f> oldPts = this.getPoints();
//
//		for ( i = 0, il = oldPts.length; i < il; i ++ ) 
//		{
//			Vector2f p = oldPts.get(i);
//
//			float oldX = p.getX();
//			float oldY = p.getY();
//
//			p.setX( a * oldX + b * oldY + c);
//			p.setY( d * oldY + e * oldX + f);
//		}
//
//		return oldPts;
//	}
//	
//
//	// Breaks path into shapes
//
//	public void toShapes() 
//	{
////		var i, il, item, action, args;
//
//		List<Path> subPaths = new ArrayList<Path>();
//				
//		Path lastPath = new Path();
//
//		for ( int i = 0, il = this.actions.size(); i < il; i ++ ) 
//		{
//			Action item = this.actions.get( i );
//
//			List<Object> args = item.args;
//			Path.PATH_ACTIONS action = item.action;
//
//			if ( action == Path.PATH_ACTIONS.MOVE_TO ) 
//			{
//				if ( lastPath.actions.size() > 0 ) 
//				{
//					subPaths.add( lastPath );
//					lastPath = new Path();
//				}
//			}
//
//			lastPath[ action ].apply( lastPath, args );
//
//		}
//
//		if ( lastPath.actions.size() != 0 )
//			subPaths.add( lastPath );
//
//		if ( subPaths.length == 0 ) 
//			return [];
//
//		var tmpPath, tmpShape, shapes = [];
//
//		var holesFirst = !THREE.Shape.Utils.isClockWise( subPaths[ 0 ].getPoints() );
//		// console.log("Holes first", holesFirst);
//
//		if ( subPaths.length == 1) 
//		{
//			tmpPath = subPaths[0];
//			tmpShape = new THREE.Shape();
//			tmpShape.actions = tmpPath.actions;
//			tmpShape.curves = tmpPath.curves;
//			shapes.push( tmpShape );
//
//			return shapes;
//		};
//
//		if ( holesFirst ) {
//
//			tmpShape = new THREE.Shape();
//
//			for ( i = 0, il = subPaths.length; i < il; i ++ ) {
//
//				tmpPath = subPaths[ i ];
//
//				if ( THREE.Shape.Utils.isClockWise( tmpPath.getPoints() ) ) {
//
//					tmpShape.actions = tmpPath.actions;
//					tmpShape.curves = tmpPath.curves;
//
//					shapes.push( tmpShape );
//					tmpShape = new THREE.Shape();
//
//					//console.log('cw', i);
//
//				} else {
//
//					tmpShape.holes.push( tmpPath );
//
//					//console.log('ccw', i);
//
//				}
//
//			}
//
//		} else {
//
//			// Shapes first
//
//			for ( i = 0, il = subPaths.length; i < il; i ++ ) {
//
//				tmpPath = subPaths[ i ];
//
//				if ( THREE.Shape.Utils.isClockWise( tmpPath.getPoints() ) ) {
//
//
//					if ( tmpShape ) shapes.push( tmpShape );
//
//					tmpShape = new THREE.Shape();
//					tmpShape.actions = tmpPath.actions;
//					tmpShape.curves = tmpPath.curves;
//
//				} else {
//
//					tmpShape.holes.push( tmpPath );
//
//				}
//
//			}
//
//			shapes.push( tmpShape );
//
//		}
//
//		//console.log("shape", shapes);
//
//		return shapes;
//	}
	
	public List<Vector2f> getTransformedSpacedPoints( boolean closedPath ) 
	{
		return getTransformedSpacedPoints(closedPath, getBends());
	}

	public List<Vector2f>  getTransformedSpacedPoints( boolean closedPath, List<CurvePath> bends ) 
	{
		List<Vector2f> oldPts = this.getSpacedPoints( closedPath );

		for ( int i = 0; i < bends.size(); i ++ )
			oldPts = getWrapPoints( oldPts, bends.get( i ) );

		return oldPts;
	}
}