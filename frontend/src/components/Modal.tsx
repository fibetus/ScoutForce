/**
 * Props for {@link Modal}.
 */
export interface ModalProps {
  /** Dialog title. */
  title: string;
  /** Supporting description shown below the title. */
  description: string;
  /** Invoked when the user activates the confirm button. */
  onConfirm: () => void;
  /** Invoked when the user activates the cancel button. */
  onCancel: () => void;
  /** Label for the confirm button. */
  confirmLabel: string;
  /** Label for the cancel button. */
  cancelLabel: string;
  /** When true, styles the confirm button as a destructive action. */
  danger?: boolean;
}

/**
 * Centered modal dialog with a confirm/cancel action pair.
 *
 * Renders over a dimmed full-screen overlay. The confirm button adopts a
 * destructive (red) style when `danger` is set; otherwise it uses the accent
 * color.
 */
export function Modal({ title, description, onConfirm, onCancel, confirmLabel, cancelLabel, danger }: ModalProps) {
  return (
    <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4">
      <div className="bg-[#14141A] rounded-2xl border border-[#26262F] max-w-md w-full p-6 space-y-4">
        <h3 className="text-xl font-bold">{title}</h3>
        <p className="text-[#A1A1AA]">{description}</p>
        <div className="flex gap-3 pt-2">
          <button
            onClick={onCancel}
            className="flex-1 px-4 py-2 bg-transparent border-2 border-[#3A3A45] hover:border-[#FF6A1A] text-[#F5F5F7] rounded-lg font-medium transition-colors"
          >
            {cancelLabel}
          </button>
          <button
            onClick={onConfirm}
            className={`flex-1 px-4 py-2 rounded-lg font-medium transition-colors ${
              danger
                ? "bg-[#E5484D] hover:bg-[#E5484D]/80 text-white"
                : "bg-[#FF6A1A] hover:bg-[#FF8033] text-white"
            }`}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
