/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven;

import java.util.*;
import java.awt.Color;

public class GameUI extends ConsoleHost implements Console.Directory {
    public final String chrid;
    public final int plid;
    public MenuGrid menu;
    public MapView map;
    public MiniMap mmap;
    public Fightview fv;
    public static final Text.Foundry errfoundry = new Text.Foundry(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14), new Color(192, 0, 0));
    private Text lasterr;
    private long errtime;
    private Window invwnd, equwnd, makewnd;
    
    static {
	addtype("gameui", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    String chrid = (String)args[0];
		    int plid = (Integer)args[1];
		    return(new GameUI(parent, chrid, plid));
		}
	    });
    }
    
    public GameUI(Widget parent, String chrid, int plid) {
	super(Coord.z, parent.sz, parent);
	this.chrid = chrid;
	this.plid = plid;
	menu = new MenuGrid(Coord.z, this);
	new Avaview(new Coord(10, 10), this, plid);
	new Bufflist(new Coord(95, 50), this);
	resize(sz);
    }
    
    static class Hidewnd extends Window {
	Hidewnd(Coord c, Coord sz, Widget parent, String cap) {
	    super(c, sz, parent, cap);
	}
	
	public void wdgmsg(Widget sender, String msg, Object... args) {
	    if((sender == this) && msg.equals("close")) {
		this.hide();
		return;
	    }
	    super.wdgmsg(sender, msg, args);
	}
    }

    public Widget makechild(String type, Object[] pargs, Object[] cargs) {
	String place = ((String)pargs[0]).intern();
	if(place == "mapview") {
	    Coord cc = (Coord)cargs[0];
	    map = new MapView(Coord.z, sz, this, cc, plid);
	    map.lower();
	    if(mmap != null)
		ui.destroy(mmap);
	    // mmap = new MiniMap(new Coord(0, sz.y - 125), new Coord(125, 125), this, map);
	    return(map);
	} else if(place == "fight") {
	    fv = (Fightview)gettype(type).create(new Coord(sz.x - Fightview.width, 0), this, cargs);
	    return(fv);
	} else if(place == "inv") {
	    invwnd = new Hidewnd(new Coord(100, 100), Coord.z, this, "Inventory");
	    Widget inv = gettype(type).create(Coord.z, invwnd, cargs);
	    invwnd.pack();
	    invwnd.visible = false;
	    return(inv);
	} else if(place == "equ") {
	    equwnd = new Hidewnd(new Coord(400, 10), Coord.z, this, "Equipment");
	    Widget equ = gettype(type).create(Coord.z, equwnd, cargs);
	    equwnd.pack();
	    equwnd.visible = false;
	    return(equ);
	} else if(place == "craft") {
	    final Widget[] mk = {null};
	    makewnd = new Window(new Coord(200, 100), Coord.z, this, "Crafting") {
		    public void wdgmsg(Widget sender, String msg, Object... args) {
			if((sender == this) && msg.equals("close")) {
			    mk[0].wdgmsg("close");
			    return;
			}
			super.wdgmsg(sender, msg, args);
		    }
		    public void cdestroy(Widget w) {
			if(w == mk[0]) {
			    ui.destroy(this);
			    makewnd = null;
			}
		    }
		};
	    mk[0] = gettype(type).create(Coord.z, makewnd, cargs);
	    makewnd.pack();
	    return(mk[0]);
	} else {
	    throw(new UI.UIException("Illegal gameui child", type, pargs));
	}
    }

    public void draw(GOut g) {
	super.draw(g);
	if(cmdline != null) {
	    drawcmd(g, new Coord(15, sz.y - 20));
	} else if(lasterr != null) {
	    if((System.currentTimeMillis() - errtime) > 3000) {
		lasterr = null;
	    } else {
		g.image(lasterr.tex(), new Coord(15, sz.y - 20));
	    }
	}
    }
    
    public void uimsg(String msg, Object... args) {
	if(msg == "err") {
	    error((String)args[0]);
	} else if(msg == "setbelt") {
	} else {
	    super.uimsg(msg, args);
	}
    }

    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == menu) {
	    wdgmsg(msg, args);
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }

    private void fitwdg(Widget wdg) {
	if(wdg.c.x < 0)
	    wdg.c.x = 0;
	if(wdg.c.y < 0)
	    wdg.c.y = 0;
	if(wdg.c.x + wdg.sz.x > sz.x)
	    wdg.c.x = sz.x - wdg.sz.x;
	if(wdg.c.y + wdg.sz.y > sz.y)
	    wdg.c.y = sz.y - wdg.sz.y;
    }

    public boolean globtype(char key, java.awt.event.KeyEvent ev) {
	if(key == ':') {
	    entercmd();
	    return(true);
	} else if(key == 9) {
	    if((invwnd != null) && (invwnd.visible = !invwnd.visible)) {
		invwnd.raise();
		fitwdg(invwnd);
	    }
	    return(true);
	} else if(key == 5) {
	    if((equwnd != null) && (equwnd.visible = !equwnd.visible)) {
		equwnd.raise();
		fitwdg(equwnd);
	    }
	    return(true);
	} else if(key == 1) {
	    wdgmsg("atkm");
	    return(true);
	}
	return(super.globtype(key, ev));
    }
    
    public void resize(Coord sz) {
	super.resize(sz);
	menu.c = sz.sub(menu.sz);
	if(map != null)
	    map.resize(sz);
	if(mmap != null)
	    mmap.c = new Coord(0, sz.y - mmap.sz.y);
    }
    
    public void presize() {
	resize(parent.sz);
    }
    
    public void error(String msg) {
	errtime = System.currentTimeMillis();
	lasterr = errfoundry.render(msg);
    }

    private Map<String, Console.Command> cmdmap = new TreeMap<String, Console.Command>();
    {
	cmdmap.put("afk", new Console.Command() {
		public void run(Console cons, String[] args) {
		    wdgmsg("afk");
		}
	    });
	cmdmap.put("act", new Console.Command() {
		public void run(Console cons, String[] args) {
		    Object[] ad = new Object[args.length - 1];
		    System.arraycopy(args, 1, ad, 0, ad.length);
		    wdgmsg("act", ad);
		}
	    });
    }
    public Map<String, Console.Command> findcmds() {
	return(cmdmap);
    }
}
