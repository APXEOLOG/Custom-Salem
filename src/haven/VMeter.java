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

import java.awt.Color;

public class VMeter extends Widget {
    private static final Coord C2 = new Coord(1, 0);
    static Tex bg = Resource.loadtex("gfx/hud/vm-frame");
    static Tex fg = Resource.loadtex("gfx/hud/vm-tex");
    Color cl;
    int amount;
    private Tex amt = null;
	
    static {
	Widget.addtype("vm", new WidgetFactory() {
		public Widget create(Coord c, Widget parent, Object[] args) {
		    Color cl;
		    if(args.length > 4) {
			cl = new Color((Integer)args[1],
				       (Integer)args[2],
				       (Integer)args[3],
				       (Integer)args[4]);
		    } else if(args.length > 3) {
			cl = new Color((Integer)args[1],
				       (Integer)args[2],
				       (Integer)args[3]);
		    } else {
			cl = (Color)args[1];
		    }
		    return(new VMeter(c, parent, (Integer)args[0], cl));
		}
	    });
    }
	
    public VMeter(Coord c, Widget parent, int amount, Color cl) {
	super(c, bg.sz().add(2, 12), parent);
	this.amount = amount;
	this.cl = cl;
    }
    
    private Tex amt(){
	if(amt == null){
	    amt = Text.render(String.format("%d", amount)).tex();
	}
	return amt;
    }
    
    public void draw(GOut g) {
	g.image(bg, C2);
	g.chcolor(cl);
	int h = (sz.y - 18);
	h = (h * amount) / 100;
	g.image(fg, C2, new Coord(0, sz.y - 15 - h), sz.add(0, h));
	g.chcolor();
	g.aimage(amt(), new Coord(sz.x/2, sz.y), 0.5, 1);
    }
	
    public void uimsg(String msg, Object... args) {
	if(msg == "set") {
	    amount = (Integer)args[0];
	    amt = null;
	} else {
	    super.uimsg(msg, args);
	}
    }
}
