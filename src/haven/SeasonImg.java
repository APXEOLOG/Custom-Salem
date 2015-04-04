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

import static haven.Window.wbox;

public class SeasonImg extends Window {
    private static final Tex seasons[] = {Resource.loadtex("gfx/hud/coldsnap"),Resource.loadtex("gfx/hud/everbloom"),Resource.loadtex("gfx/hud/bloodmoon")};
    private double t = 0;
    
    public SeasonImg(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent,null);
	synchronized (Config.window_props) {
	    try {
		this.c = new Coord(Config.window_props.getProperty("season_pos", c.toString()));
	    } catch (Exception e){}
	}
    }
    
    @Override
    public boolean mouseup(Coord c, int button) {
        boolean result = super.mouseup(c,button);
	Config.setWindowOpt("season_pos", this.c.toString());
	return(result);
    }
    
    public void draw(GOut g) {
	Tex t = seasons[ui.sess.glob.season];
        g.image(t, this.sz.sub(t.sz()).div(2));
        wbox.draw(g, Coord.z, sz);
    }
}
