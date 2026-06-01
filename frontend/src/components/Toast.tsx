import { CheckCircle2 } from "lucide-react";

/**
 * Visual severity of a {@link Toast}, which determines its background color.
 */
export type ToastType = "success" | "info" | "warning" | "danger";

/**
 * Props for {@link Toast}.
 */
export interface ToastProps {
  /** Message text displayed inside the toast. */
  message: string;
  /** Severity that selects the toast background color. */
  type: ToastType;
}

/**
 * Transient notification anchored to the top-right corner of the screen.
 *
 * Slides in via the `slideIn` keyframe and uses a background color derived
 * from the `type` severity. The caller controls mount/unmount and therefore
 * the toast's lifetime.
 */
export function Toast({ message, type }: ToastProps) {
  const colors = {
    success: 'bg-[#2EBD85]',
    info: 'bg-[#4D8DF7]',
    warning: 'bg-[#F4B740]',
    danger: 'bg-[#E5484D]'
  };

  return (
    <div className="fixed top-8 right-8 z-50 animate-[slideIn_0.2s_ease-out]">
      <div className={`${colors[type]} text-white px-6 py-4 rounded-lg shadow-2xl flex items-center gap-3 max-w-md`}>
        <CheckCircle2 className="w-5 h-5 flex-shrink-0" />
        <span className="font-medium">{message}</span>
      </div>
    </div>
  );
}
