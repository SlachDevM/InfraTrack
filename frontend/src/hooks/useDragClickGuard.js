import { useRef, useCallback } from 'react';

/**
 * Distinguishes intentional clicks from click events that fire after a drag.
 * Resets on mousedown; sets didDrag on dragstart; ignores the following click if a drag occurred.
 */
export function useDragClickGuard() {
  const didDragRef = useRef(false);

  const onMouseDown = useCallback(() => {
    didDragRef.current = false;
  }, []);

  const onDragStart = useCallback((e, onStart) => {
    didDragRef.current = true;
    onStart?.(e);
  }, []);

  const onClick = useCallback((handler) => {
    if (didDragRef.current) {
      didDragRef.current = false;
      return;
    }
    handler?.();
  }, []);

  return { onMouseDown, onDragStart, onClick };
}
