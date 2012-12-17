
package com.badlogic.gdx.backends.ios;

import cli.MonoTouch.Foundation.NSSet;
import cli.MonoTouch.GLKit.GLKView;
import cli.MonoTouch.GLKit.GLKViewController;
import cli.MonoTouch.GLKit.GLKViewDelegate;
import cli.MonoTouch.OpenGLES.EAGLContext;
import cli.MonoTouch.OpenGLES.EAGLRenderingAPI;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UIInterfaceOrientation;
import cli.MonoTouch.UIKit.UIScreen;
import cli.System.Drawing.RectangleF;
import cli.System.Drawing.Size;

import com.badlogic.gdx.Gdx;

public class IOSGLKViewController extends GLKViewController {

	EAGLContext context;

	IOSApplicationConfiguration config;
	IOSInput iosInput;
	IOSApplication iosApplication;

	Size size;

	public IOSGLKViewController (IOSApplicationConfiguration config, IOSInput iosInput, IOSApplication iosApplication) {
		this.config = config;
		this.iosInput = iosInput;
		this.iosApplication = iosApplication;
	}

	@Override
	public void ViewDidLoad () {
		super.ViewDidLoad();

		context = new EAGLContext(EAGLRenderingAPI.wrap(EAGLRenderingAPI.OpenGLES2));

		IOSMonotouchGLES20 gl20 = new IOSMonotouchGLES20();

		Gdx.gl = gl20;
		Gdx.gl20 = gl20;

		GLKView glkView = (GLKView)get_View();
		glkView.set_Context(context);
		glkView.set_MultipleTouchEnabled(true);
		glkView.set_Delegate(new GLKViewDelegate() {
			@Override
			public void DrawInRect (GLKView glkView, RectangleF rectangle) {
				Render();
			}
		});

		set_PreferredFramesPerSecond(60);
		size = UIScreen.get_MainScreen().get_Bounds().get_Size().ToSize();

		EAGLContext.SetCurrentContext(context);

		lastFrameTime = System.nanoTime();
		framesStart = lastFrameTime;
	}

	@Override
	public void ViewDidUnload () {
		super.ViewDidUnload();
		if (EAGLContext.get_CurrentContext() == context) EAGLContext.SetCurrentContext(null);
	}

	long lastFrameTime;
	float deltaTime;
	long framesStart;
	int frames;
	int fps;

	public void Render () {
		long time = System.nanoTime();
		deltaTime = (time - lastFrameTime) / 1000000000.0f;
		lastFrameTime = time;

		frames++;
		if (time - framesStart >= 1000000000l) {
			framesStart = time;
			fps = frames;
			frames = 0;
		}

		((IOSInput)Gdx.input).processEvents();
		iosApplication.listener.render();
	}

	@Override
	public void Update () {
		super.Update();
		iosApplication.processRunnables();
	}

	@Override
	public void DidRotate (UIInterfaceOrientation orientation) {

	}

	@Override
	public boolean ShouldAutorotateToInterfaceOrientation (UIInterfaceOrientation orientation) {
		switch (orientation.Value) {
		case UIInterfaceOrientation.LandscapeLeft:
		case UIInterfaceOrientation.LandscapeRight:
			return config.orientationLandscape;
		default:
			return config.orientationPortrait;
		}
	}

	@Override
	public void TouchesBegan (NSSet touches, UIEvent event) {
		super.TouchesBegan(touches, event);
		iosInput.touchDown(touches, event);
	}

	@Override
	public void TouchesCancelled (NSSet touches, UIEvent event) {
		super.TouchesCancelled(touches, event);
		iosInput.touchUp(touches, event);
	}

	@Override
	public void TouchesEnded (NSSet touches, UIEvent event) {
		super.TouchesEnded(touches, event);
		iosInput.touchUp(touches, event);
	}

	@Override
	public void TouchesMoved (NSSet touches, UIEvent event) {
		super.TouchesMoved(touches, event);
		iosInput.touchMoved(touches, event);
	}

}
