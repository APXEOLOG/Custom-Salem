package haven;

import org.ender.timer.Timer;
import org.ender.timer.TimerController;

public class TimerWdg extends Widget {

    static Tex bg = Resource.loadtex("gfx/hud/bosq");
    private Timer timer;
    public Label time, name;
    private Button start, stop, delete;
            
    public TimerWdg(Coord c, Widget parent, Timer timer) {
	super(c, bg.sz(), parent);

	this.timer = timer;
	timer.updater =  new Timer.Callback() {
	    
	    @Override
	    public void run(Timer timer) {
		synchronized (time) {
		    time.settext(timer.toString());
		    updbtns();
		}
		
	    }
	};
	name = new Label(new Coord(5,5), this, timer.getName());
	time = new Label(new Coord(5, 25), this, timer.toString());
	
	start = new Button(new Coord(125,4), 50, this, "start");
	stop = new Button(new Coord(125,4), 50, this, "stop");
	delete = new Button(new Coord(125,27), 50, this, "delete");
	updbtns();
    }
    
    private void updbtns(){
	start.visible = !timer.isWorking();
	stop.visible = timer.isWorking();
    }
    
    @Override
    public void destroy() {
	unlink();
	Window wnd = getparent(Window.class);
	if(wnd != null){
	    wnd.pack();
	}
	timer.updater = null;
	timer = null;
	super.destroy();
    }

    @Override
    public void draw(GOut g) {
	g.image(bg, Coord.z);
	super.draw(g);
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == start){
	    timer.start();
	    updbtns();
	} else if(sender == stop){
	    timer.stop();
	    updbtns();
	} else if(sender == delete){
            if(!TimerPanel.isDeletionLocked())
            {
                timer.destroy();
                TimerController.getInstance().save();
                ui.destroy(this);
            }
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }    
    

}