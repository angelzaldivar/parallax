/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
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

package thothbot.squirrel.core.client;

import java.beans.Beans;
import java.util.Iterator;

import thothbot.squirrel.core.client.context.Canvas3d;
import thothbot.squirrel.core.client.context.Canvas3dAttributes;
import thothbot.squirrel.core.client.context.Canvas3dException;
import thothbot.squirrel.core.client.renderers.WebGLRenderer;
import thothbot.squirrel.core.client.widget.BadCanvasPanel;
import thothbot.squirrel.core.client.widget.LoadingPanel;
import thothbot.squirrel.core.resources.CoreResources;
import thothbot.squirrel.core.shared.Log;
import thothbot.squirrel.core.shared.cameras.PerspectiveCamera;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;

public class RenderingPanel extends LayoutPanel implements IsWidget, HasWidgets, HasHandlers
{	
	public static class RenderPanelAttributes 
	{
		// Canvas3d attributes
		public boolean isAlphaEnabled                 = true;
		public boolean isDepthEnabled                 = true;
		public boolean isPremultipliedAlphaEnabled    = true;
		public boolean isAntialiasEnabled             = true;
		public boolean isStencilEnabled               = true;
		public boolean isPreserveDrawingBufferEnabled = false;
		
		// Rendering panel attributes
		public boolean isDebugEnabled                 = false;
	
		// Renderer attributes
		public WebGLRenderer.PRECISION precision      = WebGLRenderer.PRECISION.HIGHP;
		public int clearColor                         = 0x000000; 
		public float clearAlpha                       = 1.0f;
		public int maxLights                          = 4;
	}
	
	private RenderingScene renderingScene;
	private RenderPanelAttributes attributes;
	private HandlerManager handlerManager;
	private boolean isLoaded = false;
		
	// Debug panel
	private Debugger debugger;
	
	// Loading info panel
	private LoadingPanel loadingPanal; 

	private WebGLRenderer renderer;

	public RenderingPanel()
	{
		this(new RenderingPanel.RenderPanelAttributes());
	}
	
	public RenderingPanel(RenderPanelAttributes attributes) 
	{
		this.attributes = attributes;

		this.handlerManager = new HandlerManager(this);
		
		this.getElement().getStyle().setPosition(Position.RELATIVE);
		this.getElement().getStyle().setBackgroundColor("#" + Integer.toHexString(attributes.clearColor));

		// Loading specific styles
		CoreResources.INSTANCE.css().ensureInjected();
		
		// Add loading panel
		this.loadingPanal = new LoadingPanel();
		add(this.loadingPanal);
	}

	/*
	 * Load renderer
	 */
	private void loadRenderer() throws Canvas3dException
	{
		// Do not create WebGLRenderer instance while design UI (in Eclipse)
		// otherwise you'll see exception in UI builder
		// Also Load renderer when needed
		if (!Beans.isDesignTime() && this.renderer == null) 
		{
			Log.debug("RenderingPanel: initRenderer()");

			this.renderer = new WebGLRenderer(loadCanvas());
			this.renderer.setClearColorHex(this.attributes.clearColor, this.attributes.clearAlpha);
			this.renderer.setPrecision(this.attributes.precision);
			this.renderer.setMaxLights(this.attributes.maxLights);
		}
	}
	
	/*
	 * Load canvas widget
	 * 
	 * @throws Canvas3dException
	 */
	private Canvas3d loadCanvas() throws Canvas3dException
	{
		Log.debug("RenderingPanel: loadCanvas()");
		
		Canvas3dAttributes context3dAttributes = new Canvas3dAttributes();
		context3dAttributes.setAlphaEnable(this.attributes.isAlphaEnabled);
		context3dAttributes.setDepthEnable(this.attributes.isDepthEnabled);
		context3dAttributes.setStencilEnable(this.attributes.isStencilEnabled);
		context3dAttributes.setAntialiasEnable(this.attributes.isAntialiasEnabled);
		context3dAttributes.setPremultipliedAlphaEnable(this.attributes.isPremultipliedAlphaEnabled);
		context3dAttributes.setPreserveDrawingBufferEnable(this.attributes.isPreserveDrawingBufferEnabled);

		Canvas3d canvas = new Canvas3d(context3dAttributes); 
		this.add(canvas);

		return canvas;
	}
	
	/*
	 * Load Debuger
	 */
	private void loadDebuger()
	{
		// also init debugger
		if(this.attributes.isDebugEnabled && this.debugger == null)
		{
			Log.debug("RenderingPanel: initDebuger()");
			this.debugger = new Debugger(getRenderer().getInfo());
			this.add(this.debugger);
			this.setWidgetRightWidth(this.debugger, 1, Unit.PX, 15, Unit.EM);
			this.setWidgetTopHeight(this.debugger, 1, Unit.PX, 2.3, Unit.EM);			
		}
	}

	public RenderingScene getRenderingScene()
	{
		return this.renderingScene;
	}
	
	public void setRenderingScene(RenderingScene renderingScene)
	{
		this.renderingScene = renderingScene;
	}


	/*
	 * This method is called when a widget is attached to the browser's document. 
	 * Canvas3d should be initialized here.
	 */
	@Override
	public void onLoad()
	{
		Log.debug("RenderingPanel: onLoad()");
		
		this.loadingPanal.show();

		super.onLoad();

		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() 
			{
				try
				{
					loadRenderer();
				}
				catch (Canvas3dException ex)
				{
					Log.error(ex.getMessage(), ex.fillInStackTrace());
					add(new BadCanvasPanel(ex.getMessage()));
				}

				onLoaded();
			}
		});
	}
	
	@Override
	protected void onUnload()
	{
		if(getRenderingScene() != null)
			getRenderingScene().stop();
		isLoaded = false;
		super.onUnload();
	}
	
	/*
	 * This method is called when a widget is fully initialized.
	 */
	public void onLoaded()
	{
		if(isLoaded) return;

		Log.debug("RenderingPanel: onLoaded()");

		isLoaded = true;

		setSize(this.getOffsetWidth(), this.getOffsetHeight());
				
		if(getRenderingScene() != null)
		{
			this.renderingScene.init(getRenderer(), new RenderingScene.RenderingSceneCallback() {
				
				@Override
				public void onUpdate()
				{
					if(debugger != null)
						debugger.update();
				}
			});
			handlerManager.fireEvent(new RenderingReadyEvent());

			loadDebuger();

			getRenderingScene().run();
			
			// Remove loading panel
			this.loadingPanal.hide();
		}
	}

	/*
	 * This method is called when the implementor's size has been modified.
	 */
	@Override
	public void onResize() 
	{
		Log.debug("RenderingPanel: onResize()");

		Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
			@Override
			public void execute() 
			{
				setSize(RenderingPanel.this.getOffsetWidth(), RenderingPanel.this.getOffsetHeight());

				if(renderingScene != null && renderingScene.getCamera() != null && renderingScene.getCamera() instanceof PerspectiveCamera )
				{
					PerspectiveCamera pCamera = (PerspectiveCamera)renderingScene.getCamera();
					pCamera.setAspectRatio(renderingScene.getRenderer().getCanvas().getAspectRation());
				}
			}
		});
	}
	
	public HandlerRegistration addAnimationReadyEventHandler(RenderingReadyHandler handler) 
	{
		Log.debug("RenderingPanel: Registered event for class " + handler.getClass().getName());

		return handlerManager.addHandler(RenderingReadyEvent.TYPE, handler);
	}

	/* 
	 * Get {@link WebGLRenderer}
	 * @return {@link WebGLRenderer}
	 */
	public WebGLRenderer getRenderer()
	{
		return this.renderer;
	}

	/*
	 * Resizes the canvas and renderer viewport to (width, height), and also sets the viewport
	 * to fit that size, starting in (0, 0).
	 * @param width
	 * 			the new width of the panel.
	 * @param height 
	 * 			the new height of the panel.
	 */
	public void setSize(int width, int height) 
	{
		Log.debug("RenderingPanel: set size: W=" + width + ", H=" + height); 
		getRenderer().setSize(width, height);

		if(this.renderingScene != null && renderingScene.getRenderer() != null)
			renderingScene.getRenderer().setSize(width, height);
	}
}