import { useState, type ImgHTMLAttributes } from "react";

/**
 * Props for {@link ImageWithFallback}.
 *
 * Extends the native `<img>` attributes so callers can pass `src`, `alt`,
 * `className`, `style` and any other standard image attribute, which are
 * forwarded to the rendered element.
 */
export interface ImageWithFallbackProps
  extends ImgHTMLAttributes<HTMLImageElement> {}

/**
 * Image component that renders a graceful placeholder when the source fails
 * to load.
 *
 * On a load error it swaps the broken image for a neutral, inlined SVG
 * placeholder while preserving the original `src` on a `data-original-url`
 * attribute for debugging. Remaining props are spread onto the underlying
 * element so it behaves like a drop-in replacement for a plain `<img>`.
 */
export function ImageWithFallback(props: ImageWithFallbackProps) {
  const [didError, setDidError] = useState(false);
  const { src, alt, style, className, ...rest } = props;

  return didError ? (
    <div
      className={`inline-block bg-gray-100 text-center align-middle ${className ?? ''}`}
      style={style}
    >
      <div className="flex items-center justify-center w-full h-full">
        <img src="data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODgiIGhlaWdodD0iODgiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgc3Ryb2tlPSIjMDAwIiBzdHJva2UtbGluZWpvaW49InJvdW5kIiBvcGFjaXR5PSIuMyIgZmlsbD0ibm9uZSIgc3Ryb2tlLXdpZHRoPSIzLjciPjxyZWN0IHg9IjE2IiB5PSIxNiIgd2lkdGg9IjU2IiBoZWlnaHQ9IjU2IiByeD0iNiIvPjxwYXRoIGQ9Im0xNiA1OCAxNi0xOCAzMiAzMiIvPjxjaXJjbGUgY3g9IjUzIiBjeT0iMzUiIHI9IjciLz48L3N2Zz4KCg==" alt="Error loading image" {...rest} data-original-url={src} />
      </div>
    </div>
  ) : (
    <img src={src} alt={alt} className={className} style={style} {...rest} onError={() => setDidError(true)} />
  );
}
