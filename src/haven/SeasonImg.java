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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class SeasonImg extends Window {
    private static final SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.ENGLISH);
    private long time;
    private static long EPOCH;
    private Tex timeTex;
    private static final Tex seasons[] = {
        Resource.loadtex("gfx/hud/coldsnap"),
        Resource.loadtex("gfx/hud/everbloom"),
        Resource.loadtex("gfx/hud/bloodmoon")
    };
    private double t = 0;
    static {
	Calendar c = new GregorianCalendar(1631, 0, 1);//Year when Providence was established
	c.setTimeZone(TimeZone.getTimeZone("GMT"));
	EPOCH = c.getTimeInMillis();
    }
    
    public SeasonImg(Coord c, Coord sz, Widget parent) {
	super(c, sz, parent,null);
	synchronized (Config.window_props) {
	    try {
		this.c = new Coord(Config.window_props.getProperty("season_pos", c.toString()));
	    } catch (Exception e){}
	}
	datef.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    @Override
    public boolean type(char key, java.awt.event.KeyEvent ev) {
	if(key == 27) {
	    return(true);
	}
	return(super.type(key, ev));
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

    @Override
    public Object tooltip(Coord c, Widget prev) {
	long stime = ui.sess.glob.globtime();
	if ((int) (time / 1000) != (int) (stime / 1000) || timeTex == null) {
	    time = stime;
	    if (timeTex != null) {
		timeTex.dispose();
	    }
	    Date date = new Date(time + EPOCH);
	    timeTex = Text.render(datef.format(date)).tex();
	}

	return timeTex;
    }
}
