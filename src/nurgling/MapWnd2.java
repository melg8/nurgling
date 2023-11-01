package nurgling;


import haven.*;
import haven.MiniMap.MapLocator;
import nurgling.minimap.NPMarker;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static haven.MCache.cmaps;
import static haven.MCache.tilesz;

public class MapWnd2 extends MapWnd {
    private boolean switching = true;
    private final Map<Long, GobMarker> markers = new HashMap<>();
    
    public MapWnd2(MapFile file, MapView mv, Coord sz, String title) {
	super(file, mv, sz, title);
    }

    private boolean compact() {
	return tool != null && !tool.visible;
    }
    
    @Override
    public void compact(boolean a) {
	switching = true;
	super.compact(a);
	switching = false;
    }
    
    @Override
    public void resize(Coord sz) {
	super.resize(sz);
    }

	public void addMarkerAtPlayer(String name, Color color) {
		MapLocator player_locator = new MapLocator(this.mv);
		Coord at = player_locator.locate(file).tc;
		MapFile.Marker nm = new NPMarker(view.sessloc.seg.id, at, name, color);
		file.add(nm);
		domark = false;
	}

    
    public void addMarker(Gob gob) {
	addMarker(gob.rc.floor(tilesz),""/* gob.tooltip()*/);
    }
    
    public void addMarker(Coord at) {
	addMarker(at, "New marker");
    }
    
    public void addMarker(Coord at, String name) {
	at = at.add(view.sessloc.tc);
	MapFile.Marker nm = new PMarker(view.sessloc.seg.id, at, name, BuddyWnd.gc[new Random().nextInt(BuddyWnd.gc.length)]);
	file.add(nm);
	focus(nm);
	if(ui.modctrl) {
	    NUtils.getGameUI().track(nm);
	}
	domark = false;
    }
    
    public void removeMarker(MapFile.Marker marker) {
	if(tool.list.sel != null && tool.list.sel.mark == marker) {
	    if(mremove != null) {
		mremove.click();
	    } else {
		view.file.remove(marker);
		NUtils.getGameUI().untrack(marker);
	    }
	}
    }
    
    public void updateGobMarkers() {
	Map<Long, GobMarker> markers;
	synchronized (this.markers) {
	    markers = new HashMap<>(this.markers);
	}
	markers.values().forEach(GobMarker::update);
    }
    
    public void track(Gob gob) {
	GobMarker marker;
	synchronized (this.markers) {
	    if(markers.containsKey(gob.id)) {
		marker = markers.get(gob.id);
	    } else {
		marker = new GobMarker(gob);
		markers.put(gob.id, marker);
	    }
	}
	NUtils.getGameUI().track(marker);
	domark = false;
    }
    
    public void untrack(long gobid) {
	synchronized (markers) {
	    markers.remove(gobid);
	}
    }
    
    public void markobj(String icon, String name, Coord2d mc) {
	synchronized (deferred) {
	    deferred.add(() -> {
		final Coord tc = mc.floor(tilesz);
		MCache.Grid obg = ui.sess.glob.map.getgrid(tc.div(cmaps));
		if(!view.file.lock.writeLock().tryLock())
		    throw (new Loading());
		try {
		    MapFile.GridInfo info = view.file.gridinfo.get(obg.id);
		    if(info == null)
			throw (new Loading());
		    Coord sc = tc.add(info.sc.sub(obg.gc).mul(cmaps));
		    //Check for duplicate
		    for (final MapFile.Marker mark : view.file.markers) {
			if(mark instanceof CustomMarker) {
			    if(mark.seg == info.seg && sc.equals(mark.tc) && icon.equals(((CustomMarker) mark).res.name)) {
				return;
			    }
			}
		    }
		    
		    final MapFile.Marker mark = new CustomMarker(info.seg, sc, name, Color.WHITE, new Resource.Spec(Resource.remote(), icon));
		    view.file.add(mark);
		} finally {
		    view.file.lock.writeLock().unlock();
		}
	    });
	}
    }
    
    public void markobj(AutoMarkers.Mark mark, Coord2d mc) {
	markobj(mark.res, mark.name, mc);
    }
    
    public class GobMarker extends MapFile.Marker {
	public final long gobid;
	public final Indir<Resource> res;
	private Coord2d rc = null;
	public final Color col;
	
	public GobMarker(Gob gob) {
	    super(0, gob.rc.floor(tilesz), /*gob.tooltip()*/"");
	    this.gobid = gob.id;
	    GobIcon icon = gob.getattr(GobIcon.class);
	    res = (icon == null) ? null : icon.res;
	    col = color(gob);
	}
	
	private Color color(Gob gob) {
//	    if(gob.isTag(no)) {
//		return new Color(220, 100, 100);
//	    } else if(gob.is(GobTag.FRIEND)) {
//		return new Color(100, 220, 100);
//	    } else if(gob.is(GobTag.ANIMAL)) {
//		return new Color(100, 200, 220);
//	    }
	    return Color.LIGHT_GRAY;
	}
    
	public void update() {
	    Gob gob = ui.sess.glob.oc.getgob(gobid);
	    if(gob != null) {
		seg = view.sessloc.seg.id;
		try {
		    rc = gob.rc.add(view.sessloc.tc.mul(tilesz));
		    tc = rc.floor(tilesz);
		} catch (Exception ignore) {}
	    }
	}
    
	public Coord2d rc() {
	    try {
		return rc.sub(view.sessloc.tc.mul(tilesz));
	    } catch (Exception ignore) {}
	    return null;
	}
    
	public boolean hide() {
	    Gob gob = ui.sess.glob.oc.getgob(gobid);
	    Gob player = NUtils.getGameUI().map.player();
	    if(gob != null && player != null) {
		if(gob.id == NUtils.getGameUI().drives) {
		    return true;
		}
//		if(gob.moving instanceof Following) {
//		    return ((Following) gob.moving).tgt == gob.glob.sess.ui.gui.map.plgob;
//		} else if(gob.moving instanceof Homing) {
//		    Homing homing = (Homing) gob.moving;
//		    return homing.dist < 30 && homing.tgt == gob.glob.sess.ui.gui.map.plgob;
//		}
//		return gob.is(GobTag.VEHICLE) && gob.rc.dist(player.rc) < 25;
	    }
	    return false;
	}
    
	@Override
	public void draw(GOut g, Coord c, Text tip, float scale, MapFile file) {
	
	}
    
	@Override
	public Area area() {
	    return null;
	}
    
	@Override
	public int hashCode() {
	    return Objects.hash(gobid);
	}
    }

	public long playerSegmentId() {
		MiniMap.Location sessloc = view.sessloc;
		if(sessloc == null) {return 0;}
		return sessloc.seg.id;
	}

	public MiniMap.Location playerLocation() {
		return view.sessloc;
	}

	public Coord2d findMarkerPosition(String name) {
		MiniMap.Location sessloc = view.sessloc;
		if(sessloc == null) {return null;}
		for (Map.Entry<Long, MapFile.SMarker> e : file.smarkers.entrySet()) {
			MapFile.SMarker m = e.getValue();
			if(m.seg == sessloc.seg.id && m.nm.contains(name)) {
				return m.tc.sub(sessloc.tc).mul(tilesz);
			}
		}
		return null;
	}
}
