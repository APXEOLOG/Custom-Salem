package haven;

import java.awt.image.BufferedImage;
import org.ender.timer.Timer;
import org.ender.timer.TimerController;

public class TimerPanel extends Window {
    
    private static TimerPanel instance;
    private Button btnnew;
    private IButton lockbtn;
    private Label lockstate;
            
    private static final BufferedImage ilockc = Resource.loadimg("gfx/hud/lockc");
    private static final BufferedImage ilockch = Resource.loadimg("gfx/hud/lockch");
    private static final BufferedImage ilocko = Resource.loadimg("gfx/hud/locko");
    private static final BufferedImage ilockoh = Resource.loadimg("gfx/hud/lockoh");
    private static final String OPT_LOCKED = "_locked";
    boolean locked;
        
    public static TimerPanel getInstance()
    {
        return instance;
    }
    
    public static void toggle(){
	if(instance == null){
	    instance = new TimerPanel(UI.instance.gui);
	} else {
	    UI.instance.destroy(instance);
	}
    }
    
    private TimerPanel(Widget parent){
	super(new Coord(250, 100), Coord.z, parent, "Timers");
	justclose = true;
	btnnew = new Button(Coord.z, 100, this, "Add timer");
	
	lockbtn = new IButton(Coord.z, this, locked?ilockc:ilocko, locked?ilocko:ilockc, locked?ilockch:ilockoh) {
	    public void click() {
		locked = !locked;
		if(locked) {
		    up = ilockc;
		    down = ilocko;
		    hover = ilockch;
		} else {
		    up = ilocko;
		    down = ilockc;
		    hover = ilockoh;
		}
		storeOpt(OPT_LOCKED, locked);
                updateLockedLabel(locked);
	    }
	};
	lockbtn.recthit = true;
        lockstate = new Label(Coord.z, this, "");
        updateLockedLabel(locked);
        
	synchronized (TimerController.getInstance().lock){
	    for(Timer timer : TimerController.getInstance().timers){
		new TimerWdg(Coord.z, this, timer);
	    }
	}
	pack();
    }

    private void updateLockedLabel(boolean locked)
    {
        lockstate.settext(locked?"Timers protected from deletion":"Timers can be deleted");
    }
    
    public boolean isDeletionLocked()
    {
        return locked;
    }
    
    @Override
    protected  void loadOpts() {
	super.loadOpts();
	synchronized (Config.window_props) {
	    locked = getOptBool(OPT_LOCKED, false);
	}
    }
    
    @Override
    public void pack() {
	int n, i=0, h = 0;
	synchronized (TimerController.getInstance().lock){
	    n = TimerController.getInstance().timers.size();
	}
	n = (int) Math.ceil(Math.sqrt((double)n/3));
	for(Widget wdg = child; wdg != null; wdg = wdg.next) {
	    if(!(wdg instanceof TimerWdg))
		continue;
	    wdg.c = new Coord((i%n)*wdg.sz.x, ((int)(i/n))*wdg.sz.y);
	    h = wdg.c.y + wdg.sz.y;
	    i++;
	}
	
	btnnew.c = new Coord(0,h+4);
	lockbtn.c = new Coord(btnnew.sz.x+4,h+6);
	lockstate.c = new Coord(btnnew.sz.x+24,h+6);
	super.pack();
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == btnnew){
	    new TimerAddWdg(c, ui.root, this);
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
    
    @Override
    public void destroy() {
	instance = null;
	super.destroy();
    }

    class TimerAddWdg extends Window{
	
	private TextEntry name, hours, minutes, seconds;
	private Button btnadd;
	private TimerPanel panel;
	
	public TimerAddWdg(Coord c, Widget parent, TimerPanel panel) {
	    super(c, Coord.z, parent, "Add timer");
	    justclose = true;
	    this.panel = panel;
	    name = new TextEntry(Coord.z, new Coord(150,18), this, "timer");
	    new Label(new Coord(0, 25),this,"hours");
	    new Label(new Coord(50, 25),this,"min");
	    new Label(new Coord(100, 25),this,"sec");
	    hours = new TextEntry(new Coord(0, 40), new Coord(45,18), this, "0");
	    minutes = new TextEntry(new Coord(50, 40), new Coord(45,18), this, "00");
	    seconds = new TextEntry(new Coord(100, 40), new Coord(45,18), this, "00");
	    btnadd = new Button(new Coord(0, 60), 100, this, "Add");
	    pack();
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
	    if(sender == btnadd){
		try{
		    long time = 0;
		    time += Integer.parseInt(seconds.text);
		    time += Integer.parseInt(minutes.text)*60;
		    time += Integer.parseInt(hours.text)*3600;
		    Timer timer = new Timer();
		    timer.setDuration(1000*time);
		    timer.setName(name.text);
		    TimerController.getInstance().add(timer);
		    new TimerWdg(Coord.z, panel, timer);
		    panel.pack();
		    ui.destroy(this);
		} catch(Exception e){
		    System.out.println(e.getMessage());
		    e.printStackTrace();
		}
	    } else {
		super.wdgmsg(sender, msg, args);
	    }
	}

	@Override
	public void destroy() {
	    panel = null;
	    super.destroy();
	}
	
    }

    public static void close() {
	if(instance != null){
	    instance.destroy();
	}
    }
}
