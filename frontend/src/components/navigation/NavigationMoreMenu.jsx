import { useEffect, useRef, useState } from 'react';

export default function NavigationMoreMenu({ items, onNavigate }) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);

  useEffect(() => {
    if (!open) {
      return undefined;
    }

    const handleClickOutside = (event) => {
      if (!containerRef.current?.contains(event.target)) {
        setOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  if (items.length === 0) {
    return null;
  }

  return (
    <div className="navbar-more" ref={containerRef}>
      <button
        type="button"
        className="navbar-more-button"
        aria-expanded={open}
        aria-haspopup="menu"
        onClick={() => setOpen((previous) => !previous)}
      >
        More
      </button>
      {open && (
        <div className="navbar-more-menu" role="menu">
          {items.map((item) => (
            <button
              key={item.path}
              type="button"
              role="menuitem"
              className="navbar-more-item"
              onClick={() => {
                setOpen(false);
                onNavigate(item.path);
              }}
            >
              {item.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
